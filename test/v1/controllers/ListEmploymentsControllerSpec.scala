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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.mvc.Result
import v1.controllers.validators.MockListEmploymentsValidatorFactory
import v1.fixtures.ListEmploymentsControllerFixture.mtdResponse
import v1.mocks.services.MockListEmploymentsService
import v1.models.request.listEmployments.ListEmploymentsRequest
import v1.models.response.listEmployment.{Employment, ListEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListEmploymentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockListEmploymentsService
    with MockListEmploymentsValidatorFactory {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestData: ListEmploymentsRequest = ListEmploymentsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val hmrcEmploymentModel = Employment(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    employerName = "Vera Lynn",
    dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z"))
  )

  private val customEmploymentModel = Employment(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
    employerName = "Vera Lynn"
  )

  private val listEmploymentsResponseModel = ListEmploymentResponse(
    Some(Seq(hmrcEmploymentModel, hmrcEmploymentModel)),
    Some(Seq(customEmploymentModel, customEmploymentModel))
  )

  "ListEmploymentsController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListEmploymentsService
          .listEmployments(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, listEmploymentsResponseModel))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse(employmentId))
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

        MockListEmploymentsService
          .listEmployments(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new ListEmploymentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListEmploymentsValidatorFactory,
      service = mockListEmploymentsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.listEmployments(nino, taxYear)(fakeGetRequest)
  }

}
