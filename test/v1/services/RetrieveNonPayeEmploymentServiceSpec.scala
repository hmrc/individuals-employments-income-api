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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v1.mocks.connectors.MockRetrieveNonPayeEmploymentConnector
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

import scala.concurrent.Future

class RetrieveNonPayeEmploymentServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"
  private val source  = MtdSourceEnum.latest

  private val requestData = RetrieveNonPayeEmploymentIncomeRequest(Nino(nino), TaxYear.fromMtd(taxYear), source)

  trait Test extends MockRetrieveNonPayeEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrieveNonPayeEmploymentService =
      new RetrieveNonPayeEmploymentService(connector = mockRetrieveNonPayeEmploymentConnector)

  }

  "RetrieveNonPayeEmploymentService" when {
    "retrieveNonPayeEmployment" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        MockRetrieveNonPayeEmploymentConnector
          .retrieveNonPayeEmployment(requestData)
          .returns(Future.successful(outcome))

        await(service.retrieveNonPayeEmployment(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveNonPayeEmploymentConnector
              .retrieveNonPayeEmployment(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieveNonPayeEmployment(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_VIEW"              -> InternalError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "SERVER_ERROR"              -> InternalError
        )

        val extraTysErrors = List(
          "NOT_FOUND" -> NotFoundError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
