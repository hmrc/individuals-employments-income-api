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

package v2.deleteNonPayeEmploymentIncome

import api.config.MockAppConfig
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v2.deleteNonPayeEmploymentIncome.model.request.DeleteNonPayeEmploymentIncomeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteNonPayeEmploymentIncomeControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteNonPayeEmploymentIncomeValidatorFactory
    with MockDeleteNonPayeEmploymentIncomeService
    with MockAuditService
    with MockAppConfig {

  val taxYear: String = "2020-21"

  val requestData: DeleteNonPayeEmploymentIncomeRequest = DeleteNonPayeEmploymentIncomeRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteNonPayeEmploymentController" when {
    "return a successful response with status 204 (No Content)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteNonPayeEmploymentService
          .deleteNonPayeEmployment(requestData)
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

        MockDeleteNonPayeEmploymentService
          .deleteNonPayeEmployment(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DeleteNonPayeEmploymentIncomeController = new DeleteNonPayeEmploymentIncomeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteNonPayeEmploymentIncomeValidatorFactory,
      service = mockDeleteNonPayeEmploymentIncomeService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.delete(validNino, taxYear)(
      fakeRequest.withHeaders(
        HeaderNames.AUTHORIZATION -> "Bearer Token"
      ))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteNonPayeEmploymentIncome",
        transactionName = "delete-non-paye-employment-income",
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

  }

}
