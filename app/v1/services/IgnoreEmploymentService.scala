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

import cats.implicits._
import common.errors.{EmploymentIdFormatError, RuleCustomEmploymentError}
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import shared.utils.Logging
import v1.connectors.IgnoreEmploymentConnector
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IgnoreEmploymentService @Inject() (connector: IgnoreEmploymentConnector) extends BaseService with Logging {

  def ignoreEmployment(request: IgnoreEmploymentRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.ignoreEmployment(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"       -> NinoFormatError,
      "INVALID_TAX_YEAR"                -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID"           -> EmploymentIdFormatError,
      "INVALID_REQUEST_BEFORE_TAX_YEAR" -> RuleTaxYearNotEndedError,
      "CANNOT_IGNORE"                   -> RuleCustomEmploymentError,
      "NO_DATA_FOUND"                   -> NotFoundError,
      "INVALID_CORRELATIONID"           -> InternalError,
      "SERVER_ERROR"                    -> InternalError,
      "SERVICE_UNAVAILABLE"             -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    val hipErrors = Map(
      "1117" -> TaxYearFormatError,
      "1119" -> InternalError,
      "1215" -> NinoFormatError,
      "1217" -> EmploymentIdFormatError,
      "1115" -> RuleTaxYearNotEndedError,
      "5000" -> RuleTaxYearNotSupportedError,
      "1224" -> RuleCustomEmploymentError,
      "5010" -> NotFoundError
    )

    errors ++ extraTysErrors ++ hipErrors
  }

}
