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
import v2.mocks.connectors.MockAmendOtherEmploymentConnector
import v2.models.request.amendOtherEmployment._

import scala.concurrent.Future

class AmendOtherEmploymentServiceSpec extends ServiceSpec {

  "AmendOtherEmploymentService" should {
    "amendOtherEmployment" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendOtherEmploymentConnector
          .amendOtherEmployment(amendOtherEmploymentRequest)
          .returns(Future.successful(outcome))

        await(service.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendOtherEmploymentConnector
              .amendOtherEmployment(amendOtherEmploymentRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("UNPROCESSABLE_ENTITY", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockAmendOtherEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    private val nino        = Nino("AA112233A")
    private val taxYear     = TaxYear.fromMtd("2019-20")
    private val requestBody = AmendOtherEmploymentRequestBody(None, None, None, None, None)

    val amendOtherEmploymentRequest: AmendOtherEmploymentRequest = AmendOtherEmploymentRequest(
      nino = nino,
      taxYear = taxYear,
      body = requestBody
    )

    val service: AmendOtherEmploymentService = new AmendOtherEmploymentService(
      connector = mockAmendOtherEmploymentConnector
    )

  }

}
