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
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.controllers.validators.MockIgnoreEmploymentValidatorFactory
import v1.mocks.services.MockIgnoreEmploymentService
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IgnoreEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockIgnoreEmploymentService
    with MockIgnoreEmploymentValidatorFactory
    with MockAppConfig {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new IgnoreEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockIgnoreEmploymentValidatorFactory,
      service = mockIgnoreEmploymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.ignoreEmployment(validNino, taxYear, employmentId)(fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  ))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "IgnoreEmployment",
        transactionName = "ignore-employment",
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

  val requestData: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  "IgnoreEmploymentController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockIgnoreEmploymentService
          .ignoreEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = OK)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockIgnoreEmploymentService
          .ignoreEmployment(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

}
