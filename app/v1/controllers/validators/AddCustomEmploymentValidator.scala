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

import shared.controllers.validators.resolvers.ResolveNino
import shared.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNonEmptyJsonObject, ResolveTaxYearMinimum, ResolverSupport}
import shared.models.errors.{MtdError, RuleTaxYearNotEndedError}
import cats.data.Validated
import cats.implicits._
import config.EmploymentsAppConfig
import play.api.libs.json.JsValue
import shared.models.domain.TaxYear
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}

import java.time.Clock
import scala.math.Ordered.orderingToOrdered

object AddCustomEmploymentValidator {
  private val resolveJson = ResolveNonEmptyJsonObject.resolver[AddCustomEmploymentRequestBody]
}

class AddCustomEmploymentValidator(nino: String, taxYear: String, body: JsValue, temporalValidationEnabled: Boolean, appConfig: EmploymentsAppConfig)(implicit
    clock: Clock = Clock.systemUTC)
    extends Validator[AddCustomEmploymentRequest]
    with ResolverSupport {
  import AddCustomEmploymentValidator._

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.minimumPermittedTaxYear).resolver thenValidate
      satisfies(RuleTaxYearNotEndedError)(ty => !temporalValidationEnabled || ty < TaxYear.currentTaxYear)

  override def validate: Validated[Seq[MtdError], AddCustomEmploymentRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(AddCustomEmploymentRequest) andThen AddCustomEmploymentRulesValidator.validateBusinessRules

}
