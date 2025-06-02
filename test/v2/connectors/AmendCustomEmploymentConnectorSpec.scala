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

package v2.connectors

import common.connectors.EmploymentsConnectorSpec
import common.models.domain.EmploymentId
import config.MockEmploymentsAppConfig
import play.api.Configuration
import shared.mocks.MockHttpClient
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.models.request.amendCustomEmployment.{AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

import scala.concurrent.Future

class AmendCustomEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val amendCustomEmploymentRequestBody: AmendCustomEmploymentRequestBody = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val request: AmendCustomEmploymentRequest = AmendCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId),
    body = amendCustomEmploymentRequestBody
  )

  class Test extends MockHttpClient with MockEmploymentsAppConfig {

    val connector: AmendCustomEmploymentConnector = new AmendCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      employmentsAppConfig = mockEmploymentsConfig
    )

  }

  "AmendCustomEmploymentConnector" when {
    ".amendEmployment" should {
      "return a success upon HttpClient success with hip feature switch disabled" in new Test with Release6Test {

        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1662.enabled" -> false)).once()

        willPut(
          url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom/$employmentId",
          body = amendCustomEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendEmployment(request)) shouldBe outcome
      }

      "return a success upon HttpClient success with hip feature switch enabled" in new Test with HipTest {

        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1662.enabled" -> true)).once()

        willPut(
          url = s"$baseUrl/itsd/income/employments/$nino/custom/$employmentId?taxYear=$taxYear",
          body = amendCustomEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendEmployment(request)) shouldBe outcome
      }
    }
  }

}
