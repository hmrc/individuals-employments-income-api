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

import common.models.domain.MtdSourceEnum
import play.api.Configuration
import play.api.mvc.Result
import shared.config.MockAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.controllers.validators.MockRetrieveNonPayeEmploymentIncomeValidatorFactory
import v1.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v1.mocks.services.MockRetrieveNonPayeEmploymentService
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveNonPayeEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveNonPayeEmploymentService
    with MockRetrieveNonPayeEmploymentIncomeValidatorFactory
    with MockAppConfig {

  val taxYear: String       = "2019-20"
  val source: MtdSourceEnum = MtdSourceEnum.`hmrc-held`

  val requestData: RetrieveNonPayeEmploymentIncomeRequest =
    RetrieveNonPayeEmploymentIncomeRequest(
      nino = Nino(validNino),
      taxYear = TaxYear.fromMtd(taxYear),
      source = source
    )

  "RetrieveNonPayeEmploymentIncomeController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveNonPayeEmploymentService
          .retrieveNonPayeEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

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

        MockRetrieveNonPayeEmploymentService
          .retrieveNonPayeEmployment(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveNonPayeEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveNonPayeEmploymentIncomeValidatorFactory,
      service = mockRetrieveNonPayeEmploymentService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveNonPayeEmployment(validNino, taxYear, Some(source.toString))(fakeGetRequest)
  }

}
