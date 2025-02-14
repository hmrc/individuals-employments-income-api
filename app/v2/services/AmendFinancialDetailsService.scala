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
import common.errors.{RuleInvalidSubmissionPensionSchemeError, RuleOutsideAmendmentWindowError}
import shared.controllers.RequestContext
import shared.models.errors.{InternalError, MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import v2.connectors.AmendFinancialDetailsConnector
import v2.models.request.amendFinancialDetails.AmendFinancialDetailsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendFinancialDetailsService @Inject() (connector: AmendFinancialDetailsConnector) extends BaseService {

  def amendFinancialDetails(request: AmendFinancialDetailsRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] =
    connector.amendFinancialDetails(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID"     -> NotFoundError,
      "INVALID_PAYLOAD"           -> InternalError,
      "BEFORE_TAX_YEAR_END"       -> RuleTaxYearNotEndedError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INCOME_SOURCE_NOT_FOUND"           -> NotFoundError,
      "INVALID_CORRELATION_ID"            -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED"            -> RuleTaxYearNotSupportedError,
      "OUTSIDE_AMENDMENT_WINDOW"          -> RuleOutsideAmendmentWindowError,
      "INVALID_SUBMISSION_PENSION_SCHEME" -> RuleInvalidSubmissionPensionSchemeError
    )
    errors ++ extraTysErrors
  }

}
