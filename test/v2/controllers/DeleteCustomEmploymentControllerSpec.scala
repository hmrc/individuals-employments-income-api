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

package v2.controllers

import common.models.domain.EmploymentId
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockDeleteCustomEmploymentValidatorFactory
import v2.mocks.services.MockDeleteCustomEmploymentService
import v2.models.request.deleteCustomEmployment.DeleteCustomEmploymentRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCustomEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteCustomEmploymentService
    with MockAuditService
    with MockDeleteCustomEmploymentValidatorFactory
    with MockSharedAppConfig {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestData: DeleteCustomEmploymentRequest = DeleteCustomEmploymentRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  "DeleteCustomEmploymentController" should {
    "return NO_content" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteCustomEmploymentService
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

        MockDeleteCustomEmploymentService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTestWithAudit(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteCustomEmploymentValidatorFactory,
      service = mockDeleteCustomEmploymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.deleteCustomEmployment(validNino, taxYear, employmentId)(fakeRequest.withHeaders(
      HeaderNames.AUTHORIZATION -> "Bearer Token"
    ))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteACustomEmployment",
        transactionName = "delete-a-custom-employment",
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
