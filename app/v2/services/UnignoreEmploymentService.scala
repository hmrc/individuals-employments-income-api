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
import common.errors.{EmploymentIdFormatError, RuleCustomEmploymentUnignoreError, RuleOutsideAmendmentWindowError}
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import shared.utils.Logging
import v2.connectors.UnignoreEmploymentConnector
import v2.models.request.unignoreEmployment.UnignoreEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnignoreEmploymentService @Inject() (connector: UnignoreEmploymentConnector) extends BaseService with Logging {

  def unignoreEmployment(request: UnignoreEmploymentRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.unignoreEmployment(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val ifsErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID"     -> EmploymentIdFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "CUSTOMER_ADDED"            -> RuleCustomEmploymentUnignoreError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "BEFORE_TAX_YEAR_ENDED"     -> RuleTaxYearNotEndedError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraIfsTysErrors = Map(
      "INVALID_CORRELATION_ID"    -> InternalError,
      "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
    )

    val hipErrors = Map(
      "1215" -> NinoFormatError,
      "1117" -> TaxYearFormatError,
      "1217" -> EmploymentIdFormatError,
      "1119" -> InternalError,
      "1223" -> RuleCustomEmploymentUnignoreError,
      "5010" -> NotFoundError,
      "1115" -> RuleTaxYearNotEndedError,
      "5000" -> RuleTaxYearNotSupportedError,
      "4200" -> RuleOutsideAmendmentWindowError
    )

    ifsErrors ++ extraIfsTysErrors ++ hipErrors
  }

}
