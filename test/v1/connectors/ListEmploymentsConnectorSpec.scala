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

import common.connectors.EmploymentsConnectorSpec
import config.MockEmploymentsAppConfig
import shared.mocks.MockHttpClient
import shared.models.domain.{ Nino, TaxYear, Timestamp }
import shared.models.outcomes.ResponseWrapper
import v1.models.request.listEmployments.ListEmploymentsRequest
import v1.models.response.listEmployment.{ Employment, ListEmploymentResponse }

import scala.concurrent.Future

class ListEmploymentsConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2019-20"

  val request: ListEmploymentsRequest = ListEmploymentsRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val validResponse = ListEmploymentResponse(
    employments = Some(
      Seq(
        Employment(
          employmentId = "00000000-0000-1000-8000-000000000000",
          employerName = "Vera Lynn",
          dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z"))),
        Employment(
          employmentId = "00000000-0000-1000-8000-000000000001",
          employerName = "Vera Lynn",
          dateIgnored = Some(Timestamp("2020-06-17T10:53:38.000Z")))
      )),
    customEmployments = Some(
      Seq(
        Employment(employmentId = "00000000-0000-1000-8000-000000000002", employerName = "Vera Lynn"),
        Employment(employmentId = "00000000-0000-1000-8000-000000000003", employerName = "Vera Lynn")
      ))
  )

  class Test extends MockHttpClient with MockEmploymentsAppConfig {

    val connector: ListEmploymentsConnector = new ListEmploymentsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      employmentsAppConfig = mockEmploymentsConfig
    )

  }

  "ListEmploymentsConnector" when {
    "listEmployments" must {
      "return a 200 status for a success scenario" in new Test with Release6Test {
        val outcome = Right(ResponseWrapper(correlationId, validResponse))

        willGet(
          url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear"
        ).returns(Future.successful(outcome))

        await(connector.listEmployments(request)) shouldBe outcome
      }
    }
  }

}
