/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package v1.controllers.validators

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveParsedNumber
import api.models.errors.MtdError
import cats.data.Validated
import v1.models.request.createAmendNonPayeEmployment.{CreateAmendNonPayeEmploymentRequest, CreateAmendNonPayeEmploymentRequestBody}

object CreateAmendNonPayeIncomeRulesValidator extends RulesValidator[CreateAmendNonPayeEmploymentRequest] {

  override def validateBusinessRules(parsed: CreateAmendNonPayeEmploymentRequest): Validated[Seq[MtdError], CreateAmendNonPayeEmploymentRequest] = {
    import parsed.body

    validateBody(body).onSuccess(parsed)
  }

  private def resolveNonNegativeNumber(amount: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    ResolveParsedNumber()(amount, path)

  private def validateBody(body: CreateAmendNonPayeEmploymentRequestBody) =
    resolveNonNegativeNumber(
      amount = body.tips,
      path = "/tips"
    ).toUnit

}
