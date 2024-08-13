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

import api.controllers.validators.RulesValidator
import shared.models.errors.MtdError
import cats.data.Validated
import v1.models.request.createAmendNonPayeEmployment.{CreateAmendNonPayeEmploymentRequest, CreateAmendNonPayeEmploymentRequestBody}
import v1.controllers.validators.resolvers.EmploymentsIncomeValidators._

object CreateAmendNonPayeIncomeRulesValidator extends RulesValidator[CreateAmendNonPayeEmploymentRequest] {

  override def validateBusinessRules(parsed: CreateAmendNonPayeEmploymentRequest): Validated[Seq[MtdError], CreateAmendNonPayeEmploymentRequest] = {
    import parsed.body

    validateBody(body).onSuccess(parsed)
  }

  private def validateBody(body: CreateAmendNonPayeEmploymentRequestBody) =
    validateNonNegativeNumber(
      amount = body.tips,
      path = "/tips"
    ).toUnit

}
