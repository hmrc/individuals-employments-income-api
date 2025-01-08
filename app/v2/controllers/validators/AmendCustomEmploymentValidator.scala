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

package v2.controllers.validators

import shared.controllers.validators.resolvers.ResolveNino
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNonEmptyJsonObject, ResolveTaxYearMinimum, ResolverSupport}
import shared.models.errors.{MtdError, RuleTaxYearNotEndedError}
import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import config.EmploymentsAppConfig
import shared.models.domain.TaxYear
import v2.controllers.validators.resolvers.ResolveEmploymentId
import v2.models.request.amendCustomEmployment.{AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

import java.time.Clock
import scala.math.Ordered.orderingToOrdered

object AmendCustomEmploymentValidator {
  private val resolveJson = ResolveNonEmptyJsonObject.resolver[AmendCustomEmploymentRequestBody]
}

class AmendCustomEmploymentValidator(nino: String,
                                     taxYear: String,
                                     employmentId: String,
                                     body: JsValue,
                                     temporalValidationEnabled: Boolean,
                                     appConfig: EmploymentsAppConfig)(implicit clock: Clock = Clock.systemUTC)
    extends Validator[AmendCustomEmploymentRequest]
    with ResolverSupport {
  import AmendCustomEmploymentValidator._

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver thenValidate
      satisfies(RuleTaxYearNotEndedError)(ty => !temporalValidationEnabled || ty < TaxYear.currentTaxYear)

  override def validate: Validated[Seq[MtdError], AmendCustomEmploymentRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveEmploymentId(employmentId),
      resolveJson(body)
    ).mapN(AmendCustomEmploymentRequest) andThen AmendCustomEmploymentRulesValidator.validateBusinessRules

}
