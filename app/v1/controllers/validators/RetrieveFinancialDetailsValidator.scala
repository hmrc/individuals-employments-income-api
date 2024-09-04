/*
 * Copyright 2024 HM Revenue & Customs
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

package v1.controllers.validators

import shared.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolverSupport._
import api.controllers.validators.resolvers.{ResolveTaxYearMinimum, ResolverSupport}
import api.models.domain.MtdSourceEnum
import cats.data.Validated
import cats.implicits._
import common.errors.SourceFormatError
import config.EmploymentsAppConfig
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v1.controllers.validators.resolvers.ResolveEmploymentId
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest

object RetrieveFinancialDetailsValidator {

  private val resolveSource: Resolver[Option[String], MtdSourceEnum] =
    resolvePartialFunction(SourceFormatError)(MtdSourceEnum.parser).resolveOptionallyWithDefault(MtdSourceEnum.latest)

}

class RetrieveFinancialDetailsValidator(nino: String, taxYear: String, employmentId: String, maybeSource: Option[String], appConfig: EmploymentsAppConfig)
    extends Validator[RetrieveEmploymentAndFinancialDetailsRequest]
    with ResolverSupport {
  import RetrieveFinancialDetailsValidator._

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver

  override def validate: Validated[Seq[MtdError], RetrieveEmploymentAndFinancialDetailsRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveEmploymentId(employmentId),
      resolveSource(maybeSource)
    ).mapN(RetrieveEmploymentAndFinancialDetailsRequest)

}
