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
import common.models.domain.{EmploymentId, MtdSourceEnum}
import config.MockEmploymentsAppConfig
import org.scalamock.handlers.CallHandler
import shared.connectors.DownstreamOutcome
import shared.models.domain.{Nino, TaxYear, Timestamp}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest
import v1.models.response.retrieveFinancialDetails.{Employer, Employment, RetrieveEmploymentAndFinancialDetailsResponse}

import scala.concurrent.Future

class RetrieveEmploymentAndFinancialDetailsConnectorSpec extends EmploymentsConnectorSpec {

  val nino: String = "AA123456A"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val source: MtdSourceEnum = MtdSourceEnum.latest

  val queryParams: Seq[(String, String)] = Seq(("view", source.toDesViewString))

  val employer: Employer = Employer(
    employerRef = None,
    employerName = "employer name"
  )

  val employment: Employment =
    Employment(None, None, None, None, None, None, None, None, None, None, employer = employer, None, None, None, None)

  val response: RetrieveEmploymentAndFinancialDetailsResponse = RetrieveEmploymentAndFinancialDetailsResponse(
    submittedOn = Timestamp("2020-04-04T01:01:01.000Z"),
    source = None,
    customerAdded = None,
    dateIgnored = None,
    employment = employment
  )

  "RetrieveEmploymentAndFinancialDetailsConnector" should {
    "return the expected response for a non-TYS request" when {

      "downstream returns OK" when {
        "the connector sends a valid request downstream" in new Release6Test with Test {
          val expected = Right(ResponseWrapper(correlationId, response))

          stubHttpResponse(expected)

          await(connector.retrieve(request)) shouldBe expected
        }
      }

      "downstream returns an error" in new Release6Test with Test {
        val downstreamErrorResponse: DownstreamErrors =
          DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val expected = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        stubHttpResponse(expected)

        await(connector.retrieve(request)) shouldBe expected
      }
    }

    "return the expected response for a TYS request" when {

      "downstream returns OK" when {
        "the connector sends a valid request downstream with a Tax Year Specific (TYS) tax year on IFS" in new MockEmploymentsAppConfig with TysIfsTest with Test {
          override def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
          val expected = Right(ResponseWrapper(correlationId, response))

          stubIfsHttpResponse(expected)

          await(connector.retrieve(request)) shouldBe expected
        }

        "the connector sends a valid request downstream with a Tax Year Specific (TYS) tax year on HIP" in new MockEmploymentsAppConfig with HipTest with Test {
          override def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
          val expected = Right(ResponseWrapper(correlationId, response))

          stubHipHttpResponse(expected)

          await(connector.retrieve(request)) shouldBe expected
        }
      }
    }

  }

  trait Test {
    _: ConnectorTest with MockEmploymentsAppConfig =>

    def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

    val request: RetrieveEmploymentAndFinancialDetailsRequest =
      RetrieveEmploymentAndFinancialDetailsRequest(Nino(nino), taxYear, EmploymentId(employmentId), source)

    val connector: RetrieveEmploymentAndFinancialDetailsConnector =
      new RetrieveEmploymentAndFinancialDetailsConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig,
        employmentsAppConfig = mockEmploymentsConfig)

    protected def stubHttpResponse(outcome: DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse]]]#Derived =
      willGet(
        url = s"$baseUrl/income-tax/income/employments/$nino/${taxYear.asMtd}/$employmentId",
        queryParams
      ).returns(Future.successful(outcome))

    protected def stubIfsHttpResponse(outcome: DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse])
      : CallHandler[Future[DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse]]]#Derived =
      willGet(
        url = s"$baseUrl/income-tax/income/employments/${taxYear.asTysDownstream}/$nino/$employmentId",
        queryParams
      ).returns(Future.successful(outcome))

    protected def stubHipHttpResponse(outcome: DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse])
    : CallHandler[Future[DownstreamOutcome[RetrieveEmploymentAndFinancialDetailsResponse]]]#Derived =
      willGet(
        url = s"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/income/employments/$nino/$employmentId",
        queryParams
      ).returns(Future.successful(outcome))

  }

}
