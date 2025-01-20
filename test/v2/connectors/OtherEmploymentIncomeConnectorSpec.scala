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

import config.MockFeatureSwitches
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.fixtures.OtherIncomeEmploymentFixture.retrieveResponse
import v2.models.request.otherEmploymentIncome.{DeleteOtherEmploymentIncomeRequest, RetrieveOtherEmploymentIncomeRequest}
import v2.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import scala.concurrent.Future

class OtherEmploymentIncomeConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: OtherEmploymentIncomeConnector =
      new OtherEmploymentIncomeConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

  }

  "OtherEmploymentIncomeConnector" should {
    "return a 200 result on delete" when {
      "the downstream call is successful, not tax year specific and 'isDesIf_MigrationEnabled' is off" in new DesTest
      with Test with ConnectorTest {
        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(false)
        def taxYear: TaxYear = TaxYear.fromMtd("2017-18")
        val deleteRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/$nino/2017-18") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test
      with ConnectorTest {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val deleteRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

      "the downstream call is successful, not tax year specific and 'isDesIf_MigrationEnabled' is on" in new IfsTest
      with Test with ConnectorTest {
        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(true)

        def taxYear: TaxYear = TaxYear.fromMtd("2017-18")

        val deleteRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/2017-18/income/other/employments/$nino") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome

      }
    }

    "return a 200 result on retrieve" when {
      "the downstream call is successful, is not tax year specific and 'isDesIf_MigrationEnabled' is off" in new DesTest
      with Test with ConnectorTest {
        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(false)
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        val retrieveRequest: RetrieveOtherEmploymentIncomeRequest = RetrieveOtherEmploymentIncomeRequest(
          nino = Nino(nino),
          taxYear = TaxYear.fromMtd("2021-22")
        )

        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] =
          Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/$nino/2021-22") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

      "the downstream call is successful, is not tax year specific and 'isDesIf_MigrationEnabled' is on" in new IfsTest
      with Test with ConnectorTest {
        MockFeatureSwitches.isDesIf_MigrationEnabled.returns(true)
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        val retrieveRequest: RetrieveOtherEmploymentIncomeRequest = RetrieveOtherEmploymentIncomeRequest(
          nino = Nino(nino),
          taxYear = TaxYear.fromMtd("2021-22")
        )

        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] =
          Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/${taxYear.asMtd}/income/other/employments/$nino") returns Future.successful(
          outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test
      with ConnectorTest {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val retrieveRequest: RetrieveOtherEmploymentIncomeRequest =
          RetrieveOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] =
          Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

    }
  }

}
