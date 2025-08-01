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

package v1.services

import common.errors.{EmploymentIdFormatError, RuleDeleteForbiddenError}
import common.models.domain.EmploymentId
import shared.controllers.EndpointLogContext
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.mocks.connectors.MockDeleteCustomEmploymentConnector
import v1.models.request.deleteCustomEmployment.DeleteCustomEmploymentRequest

import scala.concurrent.Future

class DeleteCustomEmploymentServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = TaxYear.fromMtd("2019-20")
  private val employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  "DeleteCustomEmploymentService" when {
    "delete" must {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockDeleteCustomEmploymentConnector
          .delete(request)
          .returns(Future.successful(outcome))

        val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.delete(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            val outcome: Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

            MockDeleteCustomEmploymentConnector
              .delete(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.delete(request))

            result shouldBe outcome
          }

        val ifsErrors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )
        val hipErrors: Seq[(String, MtdError)] = List(
          ("1117", TaxYearFormatError),
          ("1215", NinoFormatError),
          ("1217", EmploymentIdFormatError),
          ("5000", RuleTaxYearNotSupportedError),
          ("5010", NotFoundError),
          ("1222", RuleDeleteForbiddenError)
        )

        (ifsErrors ++ hipErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockDeleteCustomEmploymentConnector {

    val request: DeleteCustomEmploymentRequest = DeleteCustomEmploymentRequest(Nino(nino), taxYear, employmentId)

    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: DeleteCustomEmploymentService = new DeleteCustomEmploymentService(
      connector = mockDeleteCustomEmploymentConnector
    )

  }

}
