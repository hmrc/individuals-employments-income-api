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
import api.controllers.validators.resolvers.ResolveTaxYearMinimum
import api.controllers.validators.resolvers.ResolverSupport._
import api.models.domain.MtdSourceEnum
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import common.errors.SourceFormatError
import config.EmploymentsAppConfig
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

object RetrieveNonPayeEmploymentIncomeValidator {

  private val resolveSource: Resolver[Option[String], MtdSourceEnum] =
    resolvePartialFunction(SourceFormatError)(MtdSourceEnum.parser).resolveOptionallyWithDefault(MtdSourceEnum.latest)

}

class RetrieveNonPayeEmploymentIncomeValidator(nino: String, taxYear: String, maybeSource: Option[String], appConfig: EmploymentsAppConfig)
    extends Validator[RetrieveNonPayeEmploymentIncomeRequest] {
  import RetrieveNonPayeEmploymentIncomeValidator._

  private val resolveTaxYear = ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver

  override def validate: Validated[Seq[MtdError], RetrieveNonPayeEmploymentIncomeRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveSource(maybeSource)
    ).mapN(RetrieveNonPayeEmploymentIncomeRequest)

}
