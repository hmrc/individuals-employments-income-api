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
import mocks.MockEmploymentsAppConfig
import play.api.Configuration
import play.api.mvc.Result
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.controllers.validators.MockRetrieveOtherEmploymentValidatorFactory
import v1.fixtures.OtherIncomeEmploymentFixture.retrieveOtherResponseModel
import v1.fixtures.RetrieveOtherEmploymentControllerFixture.mtdResponse
import v1.mocks.services.MockRetrieveOtherEmploymentIncomeService
import v1.models.request.otherEmploymentIncome.RetrieveOtherEmploymentIncomeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherEmploymentControllerSpec
    extends EmploymentsControllerBaseSpec
    with EmploymentsControllerTestRunner
    with MockRetrieveOtherEmploymentIncomeService
    with MockRetrieveOtherEmploymentValidatorFactory
    with MockEmploymentsAppConfig {

  val taxYear: String = "2019-20"

  val requestData: RetrieveOtherEmploymentIncomeRequest = RetrieveOtherEmploymentIncomeRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "RetrieveOtherEmploymentIncomeController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveOtherEmploymentIncomeService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveOtherResponseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveOtherEmploymentIncomeService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends EmploymentsControllerTest {

    val controller = new RetrieveOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveOtherEmploymentValidatorFactory,
      service = mockRetrieveOtherEmploymentIncomeService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedEmploymentsAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    MockedEmploymentsAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveOther(validNino, taxYear)(fakeGetRequest)

  }

}
