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
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.models.request.deleteStudentLoanBIK.DeleteStudentLoanBIKRequest

import scala.concurrent.Future

class DeleteStudentLoanBIKConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"
  private val taxYear: String = "2024-25"
  private val downstreamTaxYear: String = "24-25"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "DeleteStudentLoansBIKConnector" should {
    "return a 204 result on delete" in new HipTest with Test {

        willDelete(url"$baseUrl/itsa/income-tax/v1/$downstreamTaxYear/student-loan/payrolled-benefits/$nino/$employmentId") returns Future
          .successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome


    }
  }

  trait Test {
    _: ConnectorTest =>

    val connector: DeleteStudentLoanBIKConnector = new DeleteStudentLoanBIKConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: DeleteStudentLoanBIKRequest =
      DeleteStudentLoanBIKRequest(
        nino = Nino(nino),
        taxYear = TaxYear.fromMtd(taxYear),
        employmentId = EmploymentId(employmentId)
      )

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

}
