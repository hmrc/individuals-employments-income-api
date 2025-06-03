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
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class IgnoreEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

    val connector: IgnoreEmploymentConnector = new IgnoreEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      employmentId = EmploymentId(employmentId)
    )

    val outcome = Right(ResponseWrapper(correlationId, ()))
  }

  "ignoreEmployment" when {
    "given a valid request" should {
      "return a success response when feature switch is disabled(IFS enabled)" in new TysIfsTest with Test with ConnectorTest {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1940.enabled" -> false)
        willPut(
          url = s"$baseUrl/income-tax/21-22/income/employments/$nino/$employmentId/ignore",
          body = ""
        ) returns Future.successful(outcome)

        private val result = await(connector.ignoreEmployment(request))
        result shouldBe outcome
      }

      "return a success response when feature switch is enabled (HIP enabled)" in new HipTest with Test with ConnectorTest {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1940.enabled" -> true)
        willPut(
          url = s"$baseUrl/itsd/income/ignore/employments/$nino/$employmentId?taxYear=${taxYear.asTysDownstream}",
          body = ""
        ) returns Future.successful(outcome)

        private val result = await(connector.ignoreEmployment(request))
        result shouldBe outcome
      }
    }
  }

}
