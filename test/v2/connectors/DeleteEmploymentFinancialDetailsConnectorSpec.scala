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

package v2.connectors

import common.connectors.EmploymentsConnectorSpec
import common.models.domain.EmploymentId
import config.MockEmploymentsAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.deleteEmploymentFinancialDetails.DeleteEmploymentFinancialDetailsRequest

import scala.concurrent.Future

class DeleteEmploymentFinancialDetailsConnectorSpec extends EmploymentsConnectorSpec {

  "DeleteEmploymentFinancialDetailsConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new Release6Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/income/employments/$nino/2019-20/$employmentId"
        ).returns(Future.successful(outcome))

        await(connector.deleteEmploymentFinancialDetails(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new MockEmploymentsAppConfig with IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = url"$baseUrl/income-tax/23-24/income/employments/$nino/$employmentId"
        ).returns(Future.successful(outcome))

        await(connector.deleteEmploymentFinancialDetails(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest with MockEmploymentsAppConfig =>

    def taxYear: TaxYear

    protected val nino: String         = "AA111111A"
    protected val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    protected val request: DeleteEmploymentFinancialDetailsRequest =
      DeleteEmploymentFinancialDetailsRequest(
        nino = Nino(nino),
        taxYear = taxYear,
        employmentId = EmploymentId(employmentId)
      )

    val connector: DeleteEmploymentFinancialDetailsConnector = new DeleteEmploymentFinancialDetailsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      employmentsAppConfig = mockEmploymentsConfig
    )

  }

}
