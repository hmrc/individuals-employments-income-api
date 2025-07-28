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
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.fixtures.studentLoansBIK.CreateAmendStudentLoansBIKConnectorFixture.requestBodyModel
import v2.mocks.connectors.MockCreateAmendStudentLoansBIKConnector
import v2.models.request.createAmendStudentLoansBIK.CreateAmendStudentLoansBIKRequest

import scala.concurrent.Future

class CreateAmendStudentLoansBIKServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = TaxYear.fromMtd("2019-20")
  private val employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  "CreateAmendStudentLoansBIKService" when {
    "createAndAmend" must {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendStudentLoansBIKConnector
          .createAndAmend(request)
          .returns(Future.successful(outcome))
        val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createAndAmend(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            val outcome: Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

            MockCreateAmendStudentLoansBIKConnector
              .createAndAmend(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createAndAmend(request))

            result shouldBe outcome
          }

        val hipErrors: Seq[(String, MtdError)] = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        hipErrors.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockCreateAmendStudentLoansBIKConnector {
    val request: CreateAmendStudentLoansBIKRequest = CreateAmendStudentLoansBIKRequest(Nino(nino), taxYear, employmentId, requestBodyModel)

    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: CreateAmendStudentLoansBIKService = new CreateAmendStudentLoansBIKService(
      connector = mockConnector
    )

  }

}
