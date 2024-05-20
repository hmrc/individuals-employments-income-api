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
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum, ResolverSupport}
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleTaxYearNotEndedError}
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v1.models.request.createAmendNonPayeEmployment.{CreateAmendNonPayeEmploymentRequest, CreateAmendNonPayeEmploymentRequestBody}

import java.time.Clock
import scala.math.Ordered.orderingToOrdered

object CreateAmendNonPayeEmploymentIncomeValidator {
  private val resolveJson = ResolveNonEmptyJsonObject.resolver[CreateAmendNonPayeEmploymentRequestBody]

}

class CreateAmendNonPayeEmploymentIncomeValidator(nino: String,
                                                  taxYear: String,
                                                  body: JsValue,
                                                  temporalValidationEnabled: Boolean = true,
                                                  appConfig: AppConfig)(implicit clock: Clock)
    extends Validator[CreateAmendNonPayeEmploymentRequest]
    with ResolverSupport {
  import CreateAmendNonPayeEmploymentIncomeValidator._

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver thenValidate
      satisfies(RuleTaxYearNotEndedError)(ty => !temporalValidationEnabled || ty < TaxYear.currentTaxYear)

  override def validate: Validated[Seq[MtdError], CreateAmendNonPayeEmploymentRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateAmendNonPayeEmploymentRequest) andThen CreateAmendNonPayeIncomeRulesValidator.validateBusinessRules

}
