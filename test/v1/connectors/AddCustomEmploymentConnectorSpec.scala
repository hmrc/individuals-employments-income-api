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

package v1.connectors

import api.connectors.EmploymentsConnectorSpec
import mocks.MockEmploymentsAppConfig
import shared.mocks.MockHttpClient
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import scala.concurrent.Future

class AddCustomEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2021-22"

  val addCustomEmploymentRequestBody: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val request: AddCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = addCustomEmploymentRequestBody
  )

  val response: AddCustomEmploymentResponse = AddCustomEmploymentResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  class Test extends MockHttpClient with MockEmploymentsAppConfig {

    val connector: AddCustomEmploymentConnector = new AddCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockEmploymentsConfig
    )

  }

  "AddCustomEmploymentConnector" when {
    ".addEmployment" should {
      "return a success upon HttpClient success" in new Test with Api1661Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        willPost(
          url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom",
          body = addCustomEmploymentRequestBody
        ).returns(Future.successful(outcome))

        await(connector.addEmployment(request)) shouldBe outcome
      }
    }
  }

}
