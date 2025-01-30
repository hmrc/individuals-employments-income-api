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

package v2.services

import common.errors.{EmploymentIdFormatError, RuleCustomEmploymentError, RuleOutsideAmendmentWindow}
import common.models.domain.EmploymentId
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.mocks.connectors.MockIgnoreEmploymentConnector
import v2.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class IgnoreEmploymentServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = TaxYear.fromMtd("2021-22")
  private val employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId
  )

  trait Test extends MockIgnoreEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: IgnoreEmploymentService = new IgnoreEmploymentService(
      connector = mockIgnoreEmploymentConnector
    )

  }

  "IgnoreEmploymentService" when {
    "ignoreEmployment" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockIgnoreEmploymentConnector
          .ignoreEmployment(request)
          .returns(Future.successful(outcome))

        await(service.ignoreEmployment(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockIgnoreEmploymentConnector
              .ignoreEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.ignoreEmployment(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
          ("CANNOT_IGNORE", RuleCustomEmploymentError),
          ("NO_DATA_FOUND", NotFoundError),
          ("INVALID_CORRELATIONID", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindow)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
