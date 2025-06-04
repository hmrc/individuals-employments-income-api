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
import play.api.Configuration
import shared.mocks.MockHttpClient
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.models.request.retrieveEmployment.RetrieveEmploymentRequest
import v2.models.response.retrieveEmployment.RetrieveEmploymentResponse

import scala.concurrent.Future

class RetrieveEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  private val nino: String    = "AA111111A"
  private val taxYear: String = "2019-20"
  private val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "retrieve" when {
    "given a valid request" must {
      "return a success response when feature switch is disabled (IFS 'release6' enabled)" in new Test with Release6Test {

        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1645.enabled" -> false)

        willGet(url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear?employmentId=$employmentId")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "return a success response when feature switch is enabled (HIP enabled)" in new Test with HipTest {

      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1645.enabled" -> true)

      willGet(
        url = s"$baseUrl/itsd/income/employments/$nino?taxYear=19-20&employmentId=$employmentId"
      ).returns(Future.successful(outcome))

      await(connector.retrieve(request)) shouldBe outcome
    }
  }

  trait Test extends MockHttpClient with MockEmploymentsAppConfig {

    val connector: RetrieveEmploymentConnector = new RetrieveEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      employmentsAppConfig = mockEmploymentsConfig
    )

    val request: RetrieveEmploymentRequest =
      RetrieveEmploymentRequest(Nino(nino), TaxYear.fromMtd(taxYear), EmploymentId(employmentId))

    val responseModel: RetrieveEmploymentResponse = RetrieveEmploymentResponse(
      employerRef = Some("123/AB56797"),
      employerName = "Employer Name Ltd.",
      startDate = Some("2020-06-17"),
      cessationDate = Some("2020-06-17"),
      payrollId = Some("123345657"),
      occupationalPension = Some(false),
      dateIgnored = None,
      submittedOn = None
    )

    val outcome: Right[Nothing, ResponseWrapper[RetrieveEmploymentResponse]] = Right(ResponseWrapper(correlationId, responseModel))
  }

}
