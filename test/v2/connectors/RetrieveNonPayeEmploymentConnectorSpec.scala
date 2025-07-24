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
import common.models.domain.MtdSourceEnum
import config.MockEmploymentsAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v2.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

import scala.concurrent.Future

class RetrieveNonPayeEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String = "AA111111A"

  "RetrieveNonPayeEmploymentConnectorSpec" when {
    "retrieveUKDividendsIncomeAnnualSummary is called" must {
      "return a 200 for success scenario" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url"$baseUrl/income-tax/income/employments/non-paye/$nino/2018-19?view=LATEST")
          .returns(Future.successful(outcome))

        await(connector.retrieveNonPayeEmployment(request)) shouldBe outcome

      }
    }

    "retrieveUkDividendsIncomeAnnualSummary is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new MockEmploymentsAppConfig with IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url"$baseUrl/income-tax/income/employments/non-paye/23-24/$nino?view=LATEST")
          .returns(Future.successful(outcome))

        await(connector.retrieveNonPayeEmployment(request)) shouldBe outcome
      }
    }
  }

  trait Test extends EmploymentsConnectorTest { _: ConnectorTest with MockEmploymentsAppConfig =>
    def taxYear: TaxYear

    protected val connector: RetrieveNonPayeEmploymentConnector =
      new RetrieveNonPayeEmploymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig, employmentsAppConfig = mockEmploymentsConfig)

    protected val request: RetrieveNonPayeEmploymentIncomeRequest =
      RetrieveNonPayeEmploymentIncomeRequest(Nino("AA111111A"), taxYear = taxYear, MtdSourceEnum.latest)

  }

}
