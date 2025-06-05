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

import cats.implicits._
import common.errors.{EmploymentIdFormatError, RuleCessationDateBeforeTaxYearStartError, RuleOutsideAmendmentWindowError, RuleStartDateAfterTaxYearEndError, RuleUpdateForbiddenError}
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import v1.connectors.AmendCustomEmploymentConnector
import v1.models.request.amendCustomEmployment.AmendCustomEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendCustomEmploymentService @Inject() (connector: AmendCustomEmploymentConnector) extends BaseService {

  def amendEmployment(request: AmendCustomEmploymentRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] =
    connector.amendEmployment(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = {
    val ifsErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID" -> EmploymentIdFormatError,
      "NOT_SUPPORTED_TAX_YEAR" -> RuleTaxYearNotEndedError,
      "INVALID_DATE_RANGE" -> RuleStartDateAfterTaxYearEndError,
      "INVALID_CESSATION_DATE" -> RuleCessationDateBeforeTaxYearStartError,
      "CANNOT_UPDATE" -> RuleUpdateForbiddenError,
      "OUTSIDE_AMENDMENT_WINDOW" -> RuleOutsideAmendmentWindowError,
      "NO_DATA_FOUND" -> NotFoundError,
      "INVALID_PAYLOAD" -> InternalError,
      "INVALID_CORRELATIONID" -> InternalError,
      "SERVER_ERROR" -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

    val hipErrors: Map[String, MtdError] = Map(
      "1000" -> InternalError,
      "1115" -> RuleTaxYearNotEndedError,
      "1116" -> RuleStartDateAfterTaxYearEndError,
      "1117" -> TaxYearFormatError,
      "1118" -> RuleCessationDateBeforeTaxYearStartError,
      "1215" -> NinoFormatError,
      "1217" -> EmploymentIdFormatError,
      "1221" -> RuleUpdateForbiddenError,
      "4200" -> RuleOutsideAmendmentWindowError,
      "5010" -> NotFoundError
    )

    ifsErrors ++ hipErrors
  }

}
