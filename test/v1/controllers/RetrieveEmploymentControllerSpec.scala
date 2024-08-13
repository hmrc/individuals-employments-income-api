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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{EmploymentId, Nino, TaxYear, Timestamp}
import shared.models.errors._
import mocks.MockAppConfig
import play.api.Configuration
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveEmploymentValidatorFactory
import v1.fixtures.RetrieveEmploymentControllerFixture._
import v1.mocks.services.MockRetrieveEmploymentService
import v1.models.request.retrieveEmployment.RetrieveEmploymentRequest
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveEmploymentService
    with MockRetrieveEmploymentValidatorFactory
    with MockAppConfig {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestData: RetrieveEmploymentRequest = RetrieveEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  private val hmrcEnteredEmploymentWithoutDateIgnoredResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = None,
    submittedOn = None
  )

  private val hmrcEnteredEmploymentWithDateIgnoredResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")),
    submittedOn = None
  )

  private val customEnteredEmploymentResponseModel = RetrieveEmploymentResponse(
    employerRef = Some("123/AB56797"),
    employerName = "Employer Name Ltd.",
    startDate = Some("2020-06-17"),
    cessationDate = Some("2020-06-17"),
    payrollId = Some("123345657"),
    occupationalPension = Some(false),
    dateIgnored = None,
    submittedOn = Some(Timestamp("2020-06-17T10:53:38.000Z"))
  )

  "RetrieveEmploymentController" should {
    "return OK" when {
      "happy path for retrieving hmrc entered employment with no date ignored present" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, hmrcEnteredEmploymentWithoutDateIgnoredResponseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdHmrcEnteredResponseWithNoDateIgnored))
      }

      "happy path for retrieving hmrc entered employment with date ignored present" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, hmrcEnteredEmploymentWithDateIgnoredResponseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdHmrcEnteredResponseWithDateIgnored))
      }

      "happy path for retrieving custom entered employment" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, customEnteredEmploymentResponseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdCustomEnteredResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveEmploymentService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveEmploymentValidatorFactory,
      service = mockRetrieveEmploymentService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveEmployment(nino, taxYear, employmentId)(fakeGetRequest)

  }

}
