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

import cats.implicits._
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import v2.connectors.OtherEmploymentIncomeConnector
import v2.models.request.otherEmploymentIncome.RetrieveOtherEmploymentIncomeRequest
import v2.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherEmploymentIncomeService @Inject() (connector: OtherEmploymentIncomeConnector) extends BaseService {

  def retrieve(request: RetrieveOtherEmploymentIncomeRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveOtherEmploymentResponse]] = {

    connector.retrieveOtherEmploymentIncome(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "NOT_FOUND"              -> NotFoundError
    )
    errors ++ extraTysErrors
  }

}
