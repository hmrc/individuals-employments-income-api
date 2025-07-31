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

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import config.EmploymentsAppConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum, ResolverSupport}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.controllers.validators.resolvers.ResolveEmploymentId
import v2.models.request.createAmendStudentLoanBIK.{CreateAmendStudentLoanBIKRequest, CreateAmendStudentLoanBIKRequestBody}

object CreateAmendStudentLoanBIKValidator {
  private val resolveJson = ResolveNonEmptyJsonObject.resolver[CreateAmendStudentLoanBIKRequestBody]

}

class CreateAmendStudentLoanBIKValidator(nino: String, taxYear: String, employmentId: String, body: JsValue, appConfig: EmploymentsAppConfig)
    extends Validator[CreateAmendStudentLoanBIKRequest]
    with ResolverSupport {
  import CreateAmendStudentLoanBIKValidator._

  private val resolveTaxYear: ResolveTaxYearMinimum = ResolveTaxYearMinimum(TaxYear.fromMtd("2025-26"))

  override def validate: Validated[Seq[MtdError], CreateAmendStudentLoanBIKRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveEmploymentId(employmentId),
      resolveJson(body)
    ).mapN(CreateAmendStudentLoanBIKRequest) andThen CreateAmendStudentLoanBIKRulesValidator.validateBusinessRules

}
