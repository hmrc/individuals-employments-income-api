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

import common.controllers.{EmploymentsControllerBaseSpec, EmploymentsControllerTestRunner}
import common.models.domain.EmploymentId
import mocks.MockEmploymentsAppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.ControllerBaseSpec
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v1.controllers.validators.MockDeleteFinancialDetailsValidatorFactory
import v1.mocks.services.MockDeleteEmploymentFinancialDetailsService
import v1.models.request.deleteEmploymentFinancialDetails.DeleteEmploymentFinancialDetailsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteEmploymentFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with EmploymentsControllerTestRunner
    with MockDeleteEmploymentFinancialDetailsService
    with MockAuditService
      with EmploymentsControllerBaseSpec
    with MockDeleteFinancialDetailsValidatorFactory
    with MockEmploymentsAppConfig {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestData: DeleteEmploymentFinancialDetailsRequest = DeleteEmploymentFinancialDetailsRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  "deleteEmploymentFinancialDetailsController" should {
    "return NO_content" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteEmploymentFinancialDetailsService
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

        MockDeleteEmploymentFinancialDetailsService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends EmploymentsControllerTest with EmploymentsAuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteEmploymentFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteFinancialDetailsValidatorFactory,
      service = mockDeleteEmploymentFinancialDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedEmploymentsAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    MockedEmploymentsAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.deleteEmploymentFinancialDetails(validNino, taxYear, employmentId)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteEmploymentFinancialDetails",
        transactionName = "delete-employment-financial-details",
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
