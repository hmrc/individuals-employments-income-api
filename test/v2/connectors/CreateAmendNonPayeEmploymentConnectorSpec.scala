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
import v2.fixtures.nonPayeEmployment.CreateAmendNonPayeEmploymentServiceConnectorFixture._
import v2.models.request.createAmendNonPayeEmployment._

import scala.concurrent.Future

class CreateAmendNonPayeEmploymentConnectorSpec extends EmploymentsConnectorSpec {

  private val nino: String = "AA111111A"

  trait Test extends EmploymentsConnectorTest { _: ConnectorTest with MockEmploymentsAppConfig =>
    def taxYear: TaxYear

    def request = CreateAmendNonPayeEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBodyModel
    )

    val connector: CreateAmendNonPayeEmploymentConnector = new CreateAmendNonPayeEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig,
      employmentsAppConfig = mockEmploymentsConfig
    )

  }

  "createAndAmend" should {
    "return a 204 status" when {
      "a valid request is made" in new Api1661Test with Test {
        lazy val taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/income/employments/non-paye/$nino/2019-20",
          body = requestBodyModel
        ).returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }

      "a valid request is made for a TYS tax year" in new MockEmploymentsAppConfig with IfsTest with Test {
        val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/income-tax/income/employments/non-paye/23-24/$nino",
          body = requestBodyModel
        ).returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }
    }
  }

}
