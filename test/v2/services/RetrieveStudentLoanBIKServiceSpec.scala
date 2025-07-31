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

package v2.services

import common.errors.{EmploymentIdFormatError, RuleOutsideAmendmentWindowError}
import common.models.domain.EmploymentId
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleIncorrectGovTestScenarioError,
  RuleTaxYearNotSupportedError,
  TaxYearFormatError
}
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.fixtures.RetrieveStudentLoanBIKFixture.responseModel
import v2.mocks.connectors.MockRetrieveStudentLoanBIKConnector
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse

import scala.concurrent.Future

class RetrieveStudentLoanBIKServiceSpec  extends ServiceSpec {

  private val retrieveRequest = RetrieveStudentLoanBIKRequest(
    nino = Nino("AA112233A"),
    taxYear = TaxYear.fromMtd("2025-26"),
    employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
  )
  "RetrieveStudentLoanBIKSpec" when {
    "the downstream retrieve request is successful" must {
      "return a success result" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveStudentLoanBIKResponse]] =
          Right(ResponseWrapper(correlationId, responseModel))

        RetrieveStudentLoanBIKConnector
          .retrieveStudentLoanBIK(retrieveRequest)
          .returns(Future.successful(outcome))

        await(retrieveService.retrieve(retrieveRequest)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit = {
          s"downstream returns $downstreamErrorCode" in new Test {
            RetrieveStudentLoanBIKConnector
              .retrieveStudentLoanBIK(retrieveRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[RetrieveStudentLoanBIKResponse]] = await(retrieveService.retrieve(retrieveRequest))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }
        }

        val hipErrors: Seq[(String, MtdError)] = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
          ("NOT_FOUND", NotFoundError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        hipErrors.foreach(args => (serviceError _).tupled(args))
      }
    }

  }

  trait Test extends MockRetrieveStudentLoanBIKConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val retrieveService: RetrieveStudentLoanBIKService =
      new RetrieveStudentLoanBIKService(mockRetrieveStudentLoanBIKConnector)

  }

}
