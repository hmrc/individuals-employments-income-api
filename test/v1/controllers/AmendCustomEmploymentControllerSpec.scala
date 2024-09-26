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

import common.models.domain.EmploymentId
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v1.controllers.validators.MockAmendCustomEmploymentValidatorFactory
import v1.mocks.services.MockAmendCustomEmploymentService
import v1.models.request.amendCustomEmployment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendCustomEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAuditService
    with MockAmendCustomEmploymentValidatorFactory
    with MockAmendCustomEmploymentService {

  val taxYear: String = "2019-20"
  val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412"
      |}
    """.stripMargin
  )

  val amendCustomEmploymentRequestBody: AmendCustomEmploymentRequestBody = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val requestData: AmendCustomEmploymentRequest = AmendCustomEmploymentRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId),
    body = amendCustomEmploymentRequestBody
  )

  "AmendCustomEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendCustomEmploymentService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendCustomEmploymentService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AmendCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendCustomEmploymentValidatorFactory,
      service = mockAmendCustomEmploymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.amendEmployment(validNino, taxYear, employmentId)(fakeRequest.withBody(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendACustomEmployment",
        transactionName = "amend-a-custom-employment",
        detail = new GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          requestBody = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
