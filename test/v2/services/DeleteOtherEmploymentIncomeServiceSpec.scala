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

package v2.services

import common.errors.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.services.ServiceSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v2.mocks.connectors.MockOtherEmploymentIncomeConnector
import v2.models.request.otherEmploymentIncome.DeleteOtherEmploymentIncomeRequest

import scala.concurrent.Future

class DeleteOtherEmploymentIncomeServiceSpec extends ServiceSpec {

  private val deleteRequest = DeleteOtherEmploymentIncomeRequest(
    nino = Nino("AA112233A"),
    taxYear = TaxYear.fromMtd("2023-24")
  )

  "OtherEmploymentIncomeSpec" when {
    "the downstream delete request is successful" must {
      "return a success result" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        OtherEmploymentIncomeConnector
          .deleteOtherEmploymentIncome(deleteRequest)
          .returns(Future.successful(outcome))

        await(deleteService.delete(deleteRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit = {

          s"downstream returns $downstreamErrorCode" in new Test {
            OtherEmploymentIncomeConnector
              .deleteOtherEmploymentIncome(deleteRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(deleteService.delete(deleteRequest))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }
        }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }

  }

  trait Test extends MockOtherEmploymentIncomeConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val deleteService: DeleteOtherEmploymentIncomeService =
      new DeleteOtherEmploymentIncomeService(otherEmploymentIncomeConnector)

  }

}
