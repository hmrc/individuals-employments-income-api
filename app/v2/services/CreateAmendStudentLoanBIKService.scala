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

import cats.implicits.toBifunctorOps
import common.errors.{EmploymentIdFormatError, RuleOutsideAmendmentWindowError}
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.connectors.CreateAmendStudentLoanBIKConnector
import v2.models.request.createAmendStudentLoanBIK.CreateAmendStudentLoanBIKRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendStudentLoanBIKService @Inject() (connector: CreateAmendStudentLoanBIKConnector) extends BaseService {

  def createAndAmend(request: CreateAmendStudentLoanBIKRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.createAndAmend(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
    "INVALID_TAX_YEAR"          -> TaxYearFormatError,
    "INVALID_EMPLOYMENT_ID"     -> EmploymentIdFormatError,
    "INVALID_PAYLOAD"           -> InternalError,
    "INVALID_CORRELATION_ID"    -> InternalError,
    "UNMATCHED_STUB_ERROR"      -> RuleIncorrectGovTestScenarioError,
    "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
    "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
  )

}
