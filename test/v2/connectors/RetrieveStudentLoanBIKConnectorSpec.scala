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
import config.MockEmploymentsAppConfig
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.mocks.MockHttpClient
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.fixtures.RetrieveStudentLoanBIKFixture.responseModel
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse

import scala.concurrent.Future

class RetrieveStudentLoanBIKConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  private val downstreamTaxYear: String = "24-25"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "RetrieveStudentLoanBIKContnectorSpec" should{
      "return a 200 for success scenario" in new HipTest with Test {

        willGet(url"$baseUrl/itsa/income-tax/v1/$downstreamTaxYear/student-loan/payrolled-benefits/$nino/$employmentId")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveStudentLoanBIKResponse] = await(connector.retrieveStudentLoanBIK(request))
        result shouldBe outcome

      }

  }

  trait Test  extends MockHttpClient with MockEmploymentsAppConfig {


    val connector: RetrieveStudentLoanBIKConnector = new RetrieveStudentLoanBIKConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: RetrieveStudentLoanBIKRequest =
      RetrieveStudentLoanBIKRequest(
        nino = Nino("AA111111A"),
        taxYear = TaxYear.fromMtd("2024-25"),
        employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
      )

    val outcome: Right[Nothing, ResponseWrapper[RetrieveStudentLoanBIKResponse]] = Right(ResponseWrapper(correlationId, responseModel))
  }
}
