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

import shared.services.MockAuditService
import shared.config.MockAppConfig
import shared.controllers.ControllerTestRunner
import shared.controllers.ControllerBaseSpec
import shared.controllers.ControllerBaseSpec
import api.models.audit.{AuditEvent, GenericAuditDetail}
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.controllers.validators.MockDeleteOtherEmploymentValidatorFactory
import v1.mocks.services.MockDeleteOtherEmploymentIncomeService
import v1.models.request.otherEmploymentIncome.DeleteOtherEmploymentIncomeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteOtherEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteOtherEmploymentValidatorFactory
    with MockDeleteOtherEmploymentIncomeService
    with MockAuditService
    with MockAppConfig {

  val taxYear: String = "2019-20"

  private val requestData: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteOtherEmploymentController" should {
    "return a successful response with status 204 (No Content)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteOtherEmploymentIncomeService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteOtherEmploymentIncomeService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteOtherEmploymentValidatorFactory,
      service = mockDeleteOtherEmploymentIncomeService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.deleteOtherEmployment(nino, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteOtherEmployment",
        transactionName = "delete-other-employment",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
