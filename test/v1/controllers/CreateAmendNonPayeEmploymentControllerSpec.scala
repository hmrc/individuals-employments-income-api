/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v1.controllers.validators.MockCreateAmendNonPayeEmploymentIncomeValidatorFactory
import v1.mocks.services.MockCreateAmendNonPayeEmploymentService
import v1.models.request.createAmendNonPayeEmployment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendNonPayeEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockSharedAppConfig
    with MockCreateAmendNonPayeEmploymentService
    with MockAuditService
    with MockCreateAmendNonPayeEmploymentIncomeValidatorFactory {

  val taxYear: String = "2019-20"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |    "tips": 100.23
      |}
      |""".stripMargin
  )

  val requestModel: CreateAmendNonPayeEmploymentRequestBody = CreateAmendNonPayeEmploymentRequestBody(
    tips = 100.23
  )

  val requestData: CreateAmendNonPayeEmploymentRequest = CreateAmendNonPayeEmploymentRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  "CreateAmendNonPayeEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendNonPayeEmploymentService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(validRequestJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(validRequestJson))

      }
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendNonPayeEmploymentService
          .createAndAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendNonPayeEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendNonPayeEmploymentIncomeValidatorFactory,
      service = mockService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.createAmendNonPayeEmployment(validNino, taxYear)(fakeRequest.withBody(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendNonPayeEmploymentIncome",
        transactionName = "create-amend-non-paye-employment",
        detail = new GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()
  }

}
