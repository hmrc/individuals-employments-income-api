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

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum, ResolverSupport}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v1.models.request.otherEmploymentIncome.OtherEmploymentIncomeRequest


class RetrieveOtherEmploymentValidator(nino: String, taxYear: String, appConfig: AppConfig)
    extends Validator[OtherEmploymentIncomeRequest]
    with ResolverSupport {

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver

  override def validate: Validated[Seq[MtdError], OtherEmploymentIncomeRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear)
    ).mapN(OtherEmploymentIncomeRequest)

}
