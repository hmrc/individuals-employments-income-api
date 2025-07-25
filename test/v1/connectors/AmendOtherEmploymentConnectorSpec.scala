/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.connectors

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{InternalError, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v1.models.request.amendOtherEmployment._

import scala.concurrent.Future

class AmendOtherEmploymentConnectorSpec extends ConnectorSpec {

  "AmendOtherEmploymentConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/income/other/employments/$nino/2019-20",
          body = amendOtherEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }

      "downstream returns a single error" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Left(ResponseWrapper(correlationId, NinoFormatError))

        willPut(
          url = url"$baseUrl/income-tax/income/other/employments/$nino/2019-20",
          body = amendOtherEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }

      "downstream returns multiple errors" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError)))

        willPut(
          url = url"$baseUrl/income-tax/income/other/employments/$nino/2019-20",
          body = amendOtherEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }
    }
    "return the expected response for a TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/income/other/employments/23-24/$nino",
          body = amendOtherEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendOtherEmployment(amendOtherEmploymentRequest)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val nino: String = "AA111111A"

    protected val amendOtherEmploymentRequestBody: AmendOtherEmploymentRequestBody =
      AmendOtherEmploymentRequestBody(None, None, None, None, None)

    protected val amendOtherEmploymentRequest: AmendOtherEmploymentRequest =
      AmendOtherEmploymentRequest(
        nino = Nino(nino),
        taxYear = taxYear,
        body = amendOtherEmploymentRequestBody
      )

    val connector: AmendOtherEmploymentConnector = new AmendOtherEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

}
