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
import config.MockEmploymentsAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.deleteNonPayeEmployment.DeleteNonPayeEmploymentRequest

import scala.concurrent.Future

class DeleteNonPayeEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String = "AA111111A"

  "DeleteNonPayeEmploymentConnectorSpec" when {
    "deleteNonPayeEmployment is called" must {
      "return a 200 for success scenario" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(url"$baseUrl/income-tax/employments/non-paye/$nino/2018-19")
          .returns(Future.successful(outcome))

        await(connector.deleteNonPayeEmployment(request)) shouldBe outcome

      }
    }

    "deleteNonPayeEmployment is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new MockEmploymentsAppConfig with IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(url"$baseUrl/income-tax/employments/non-paye/23-24/$nino")
          .returns(Future.successful(outcome))

        await(connector.deleteNonPayeEmployment(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest with MockEmploymentsAppConfig =>
    def taxYear: TaxYear

    protected val connector: DeleteNonPayeEmploymentConnector =
      new DeleteNonPayeEmploymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig, employmentsAppConfig = mockEmploymentsConfig)

    protected val request: DeleteNonPayeEmploymentRequest =
      DeleteNonPayeEmploymentRequest(Nino("AA111111A"), taxYear = taxYear)

  }

}
