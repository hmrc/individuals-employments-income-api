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
import common.errors.{EmploymentIdFormatError, RuleDeleteForbiddenError, RuleOutsideAmendmentWindowError}
import shared.controllers.RequestContext
import shared.models.errors.{MtdError, _}
import shared.services.{BaseService, ServiceOutcome}
import v1.connectors.DeleteCustomEmploymentConnector
import v1.models.request.deleteCustomEmployment.DeleteCustomEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCustomEmploymentService @Inject() (connector: DeleteCustomEmploymentConnector) extends BaseService {

  def delete(request: DeleteCustomEmploymentRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.delete(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val ifsErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID"     -> EmploymentIdFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "CANNOT_DELETE"             -> RuleDeleteForbiddenError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val hipErrors = Map(
      "1117" -> TaxYearFormatError,
      "1215" -> NinoFormatError,
      "1217" -> EmploymentIdFormatError,
      "5000" -> RuleTaxYearNotSupportedError,
      "5010" -> NotFoundError,
      "1222" -> RuleDeleteForbiddenError,
      "4200" -> RuleOutsideAmendmentWindowError
    )

    ifsErrors ++ hipErrors
  }

}
