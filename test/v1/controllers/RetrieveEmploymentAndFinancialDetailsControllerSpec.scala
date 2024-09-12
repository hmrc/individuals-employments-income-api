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

import api.models.domain.{EmploymentId, MtdSourceEnum}
import common.controllers.{EmploymentsControllerBaseSpec, EmploymentsControllerTestRunner}
import mocks.MockEmploymentsAppConfig
import play.api.Configuration
import play.api.mvc.Result
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v1.controllers.validators.MockRetrieveFinancialDetailsValidatorFactory
import v1.fixtures.RetrieveFinancialDetailsControllerFixture._
import v1.mocks.services.MockRetrieveEmploymentAndFinancialDetailsService
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEmploymentAndFinancialDetailsControllerSpec
    extends EmploymentsControllerBaseSpec
    with EmploymentsControllerTestRunner
    with MockRetrieveEmploymentAndFinancialDetailsService
    with MockRetrieveFinancialDetailsValidatorFactory
    with MockEmploymentsAppConfig {

  val taxYear: String      = "2017-18"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val source: String       = "latest"

  val requestData: RetrieveEmploymentAndFinancialDetailsRequest = RetrieveEmploymentAndFinancialDetailsRequest(
    nino = Nino(validNino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId),
    source = MtdSourceEnum.latest
  )

  "RetrieveFinancialDetailsController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentAndFinancialDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, model))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentAndFinancialDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends EmploymentsControllerTest {

    val controller = new RetrieveEmploymentAndFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveFinancialDetailsValidatorFactory,
      service = mockRetrieveEmploymentAndFinancialDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedEmploymentsAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    MockedEmploymentsAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieve(validNino, taxYear, employmentId, Some(source))(fakeRequest)
  }

}
