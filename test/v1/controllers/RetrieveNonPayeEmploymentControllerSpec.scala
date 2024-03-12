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
import api.hateoas.HateoasLinks
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v1.mocks.requestParsers.MockRetrieveNonPayeEmploymentRequestParser
import v1.mocks.services.MockRetrieveNonPayeEmploymentService
import v1.models.request.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveNonPayeEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveNonPayeEmploymentService
    with MockRetrieveNonPayeEmploymentRequestParser
    with HateoasLinks {

  val taxYear: String       = "2019-20"
  val source: MtdSourceEnum = MtdSourceEnum.hmrcHeld

  val rawData: RetrieveNonPayeEmploymentIncomeRawData =
    RetrieveNonPayeEmploymentIncomeRawData(
      nino = nino,
      taxYear = taxYear,
      source = Some(source.toString)
    )

  val requestData: RetrieveNonPayeEmploymentIncomeRequest =
    RetrieveNonPayeEmploymentIncomeRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      source = source
    )

  "RetrieveNonPayeEmploymentIncomeController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveNonPayeEmploymentService
          .retrieveNonPayeEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

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
      parser = mockRetrieveNonPayeEmploymentRequestParser,
      service = mockRetrieveNonPayeEmploymentService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveNonPayeEmployment(nino, taxYear, Some(source.toString))(fakeGetRequest)
  }

}
