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
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.controllers.validators.resolvers.CustomEmploymentDateValidator
import v2.controllers.validators.resolvers.EmploymentsIncomeValidators._
import v2.models.request.amendCustomEmployment.{AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

object AmendCustomEmploymentRulesValidator extends RulesValidator[AmendCustomEmploymentRequest] with ResolverSupport {

  def validateBusinessRules(parsed: AmendCustomEmploymentRequest): Validated[Seq[MtdError], AmendCustomEmploymentRequest] = {
    import parsed.body

    combine(
      validateCustomEmployerName(body.employerName),
      validateOptionalEmployerRef(body.employerRef),
      validatePayrollId(body.payrollId),
      validateDates(parsed.taxYear, body)
    ).onSuccess(parsed)
  }

  private def validateDates(taxYear: TaxYear, body: AmendCustomEmploymentRequestBody) = {
    resolveValid[(String, Option[String])]
      .thenValidate(CustomEmploymentDateValidator.validator(taxYear))((body.startDate, body.cessationDate))
  }

}
