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

import common.models.domain.EmploymentId
import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.deleteCustomEmployment.DeleteCustomEmploymentRequest

import scala.concurrent.Future

class DeleteCustomEmploymentConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"
  private val taxYear: String = "2019-20"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "DeleteCustomEmploymentConnector" should {
    "return a 204 result on delete" when {
      "feature switch is disabled(IFS enabled)" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1663.enabled" -> false)
        willDelete(url"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom/$employmentId") returns Future
          .successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome

      }

      "feature switch is enabled(HIP enabled)" in new HipTest with Test {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1663.enabled" -> true)

        willDelete(url"$baseUrl/itsd/income/employments/$nino/custom/$employmentId?taxYear=19-20") returns Future
          .successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome

      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    val connector: DeleteCustomEmploymentConnector = new DeleteCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: DeleteCustomEmploymentRequest =
      DeleteCustomEmploymentRequest(
        nino = Nino(nino),
        taxYear = TaxYear.fromMtd(taxYear),
        employmentId = EmploymentId(employmentId)
      )

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

}
