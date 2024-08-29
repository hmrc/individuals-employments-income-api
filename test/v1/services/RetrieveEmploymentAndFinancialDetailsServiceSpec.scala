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

package v1.services

import api.models.domain._
import shared.models.errors._
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockRetrieveEmploymentAndFinancialDetailsConnector
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest
import v1.models.response.retrieveFinancialDetails.{Employer, Employment, RetrieveEmploymentAndFinancialDetailsResponse}

import scala.concurrent.Future

class RetrieveEmploymentAndFinancialDetailsServiceSpec extends ServiceSpec {

  private val nino                 = "AA123456A"
  private val taxYear              = "2019-20"
  private val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request: RetrieveEmploymentAndFinancialDetailsRequest = RetrieveEmploymentAndFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId),
    source = MtdSourceEnum.latest
  )

  val employer: Employer = Employer(
    employerRef = None,
    employerName = "employer name"
  )

  val employment: Employment =
    Employment(None, None, None, None, None, None, None, None, None, offPayrollWorker = Some(true), employer = employer, None, None, None, None)

  val response: RetrieveEmploymentAndFinancialDetailsResponse = RetrieveEmploymentAndFinancialDetailsResponse(
    submittedOn = Timestamp("2020-04-04T01:01:01.000Z"),
    source = None,
    customerAdded = None,
    dateIgnored = None,
    employment = employment
  )

  trait Test extends MockRetrieveEmploymentAndFinancialDetailsConnector with MockAppConfig {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveEmploymentAndFinancialDetails")

    val service = new RetrieveEmploymentAndFinancialDetailsService(mockConnector, mockAppConfig)
  }

  "RetrieveEmploymentAndFinancialDetailsService" should {
    "return a success response" when {
      "downstream response is successful" in new Test {
        MockRetrieveEmploymentAndFinancialDetailsConnector
          .retrieve(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieve(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return error response" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveEmploymentAndFinancialDetailsConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
        ("INVALID_VIEW", SourceFormatError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
        ("INVALID_CORRELATIONID", InternalError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      errors.foreach(args => (serviceError _).tupled(args))
    }
  }

}
