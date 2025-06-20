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
import common.errors.{RuleCessationDateBeforeTaxYearStartError, RuleStartDateAfterTaxYearEndError}
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import v1.connectors.AddCustomEmploymentConnector
import v1.models.request.addCustomEmployment.AddCustomEmploymentRequest
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddCustomEmploymentService @Inject() (connector: AddCustomEmploymentConnector) extends BaseService {

  def addEmployment(
      request: AddCustomEmploymentRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[AddCustomEmploymentResponse]] =
    connector.addEmployment(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = {
    val ifsErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "NOT_SUPPORTED_TAX_YEAR"    -> RuleTaxYearNotEndedError,
      "INVALID_DATE_RANGE"        -> RuleStartDateAfterTaxYearEndError,
      "INVALID_CESSATION_DATE"    -> RuleCessationDateBeforeTaxYearStartError,
      "INVALID_PAYLOAD"           -> InternalError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val hipErrors = Map(
      "1215" -> NinoFormatError,
      "1117" -> TaxYearFormatError,
      "1000" -> InternalError,
      "1115" -> RuleTaxYearNotEndedError,
      "1116" -> RuleStartDateAfterTaxYearEndError,
      "1118" -> RuleCessationDateBeforeTaxYearStartError,
      "5000" -> RuleTaxYearNotSupportedError
    )

    ifsErrors ++ hipErrors
  }

}
