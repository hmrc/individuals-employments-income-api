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

package v2.retrieveOtherEmploymentIncome

import api.config.MockAppConfig
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.mvc.Result
import v2.retrieveOtherEmploymentIncome.RetrieveOtherEmploymentIncomeFixtures.{mtdResponse, retrieveOtherResponseModel}
import v2.retrieveOtherEmploymentIncome.model.request.RetrieveOtherEmploymentIncomeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherEmploymentIncomeControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherEmploymentIncomeService
    with MockRetrieveOtherEmploymentIncomeValidatorFactory
    with MockAppConfig {

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

  trait Test extends ControllerTest {

    val controller: RetrieveOtherEmploymentIncomeController = new RetrieveOtherEmploymentIncomeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveOtherEmploymentIncomeValidatorFactory,
      service = mockRetrieveOtherEmploymentIncomeService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveOther(validNino, taxYear)(fakeGetRequest)

  }

}
