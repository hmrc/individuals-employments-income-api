/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, NotFoundError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockRetrieveStudentLoanBIKValidatorFactory
import v2.fixtures.RetrieveStudentLoanBIKFixture.{mtdResponse, responseModel}
import v2.mocks.services.MockRetrieveStudentLoanBIKService
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveStudentLoanBIKControllerSpec extends ControllerBaseSpec
  with ControllerTestRunner
  with MockRetrieveStudentLoanBIKService
  with MockAuditService
  with MockRetrieveStudentLoanBIKValidatorFactory
  with MockSharedAppConfig {

  val taxYear: String = "2025-26"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestData: RetrieveStudentLoanBIKRequest = RetrieveStudentLoanBIKRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  "RetrieveStudentLoansBIKController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveStudentLoanBIKService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveStudentLoanBIKService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest  {

    val controller = new RetrieveStudentLoanBIKController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveStudentLoanBIKValidatorFactory,
      service = mockRetrieveStudentLoanBIKService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> false
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveStudentLoanBIK(validNino, taxYear, employmentId)(fakeRequest)


  }
}