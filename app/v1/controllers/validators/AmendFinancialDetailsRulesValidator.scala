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
import api.controllers.validators.resolvers._
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleMissingOffPayrollWorker, RuleNotAllowedOffPayrollWorker}
import cats.data.Validated
import v1.models.request.amendFinancialDetails.AmendFinancialDetailsRequest
import v1.models.request.amendFinancialDetails.employment.{AmendBenefitsInKind, AmendDeductions, AmendPay}

object AmendFinancialDetailsRulesValidator extends RulesValidator[AmendFinancialDetailsRequest] with ResolverSupport {

  def validateBusinessRules(parsed: AmendFinancialDetailsRequest): Validated[Seq[MtdError], AmendFinancialDetailsRequest] = {
    import parsed.body.employment

    combine(
      validatePay(employment.pay),
      validateOptionalWith(employment.deductions)(validateDeductions),
      validateOptionalWith(employment.benefitsInKind)(validateBenefitsInKind),
      validateOffPayrollWorker(parsed.taxYear, employment.offPayrollWorker)
    ).onSuccess(parsed)

  }

  private def validateOptionalWith[A](field: Option[A])(validator: A => Validated[Seq[MtdError], Unit]) =
    field match {
      case Some(value) => validator(value)
      case None        => valid
    }

  private def resolveOptionalNonNegativeNumber(amount: Option[BigDecimal], path: String): Validated[Seq[MtdError], Option[BigDecimal]] =
    ResolveParsedNumber()(amount, path)

  private def resolveNonNegativeNumber(amount: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    ResolveParsedNumber()(amount, path)

  private def resolveNumber(amount: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    ResolveParsedNumber(min = -99999999999.99)(amount, path)

  private def validatePay(pay: AmendPay) = {
    combine(
      resolveNonNegativeNumber(
        amount = pay.taxablePayToDate,
        path = "/employment/pay/taxablePayToDate"
      ),
      resolveNumber(
        amount = pay.totalTaxToDate,
        path = "/employment/pay/totalTaxToDate"
      )
    )
  }

  private def validateDeductions(deductions: AmendDeductions) = {
    combine(
      resolveOptionalNonNegativeNumber(
        amount = deductions.studentLoans.flatMap(_.pglDeductionAmount),
        path = "/employment/deductions/studentLoans/pglDeductionAmount"
      ),
      resolveOptionalNonNegativeNumber(
        amount = deductions.studentLoans.flatMap(_.uglDeductionAmount),
        path = "/employment/deductions/studentLoans/uglDeductionAmount"
      )
    )
  }

  private def validateBenefitsInKind(benefitsInKind: AmendBenefitsInKind) = {
    combine(
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.accommodation,
        path = "/employment/benefitsInKind/accommodation"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.assets,
        path = "/employment/benefitsInKind/assets"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.assetTransfer,
        path = "/employment/benefitsInKind/assetTransfer"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.beneficialLoan,
        path = "/employment/benefitsInKind/beneficialLoan"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.car,
        path = "/employment/benefitsInKind/car"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.carFuel,
        path = "/employment/benefitsInKind/carFuel"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.educationalServices,
        path = "/employment/benefitsInKind/educationalServices"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.entertaining,
        path = "/employment/benefitsInKind/entertaining"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.expenses,
        path = "/employment/benefitsInKind/expenses"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.medicalInsurance,
        path = "/employment/benefitsInKind/medicalInsurance"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.telephone,
        path = "/employment/benefitsInKind/telephone"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.service,
        path = "/employment/benefitsInKind/service"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.taxableExpenses,
        path = "/employment/benefitsInKind/taxableExpenses"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.van,
        path = "/employment/benefitsInKind/van"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.vanFuel,
        path = "/employment/benefitsInKind/vanFuel"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.mileage,
        path = "/employment/benefitsInKind/mileage"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.nonQualifyingRelocationExpenses,
        path = "/employment/benefitsInKind/nonQualifyingRelocationExpenses"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.nurseryPlaces,
        path = "/employment/benefitsInKind/nurseryPlaces"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.otherItems,
        path = "/employment/benefitsInKind/otherItems"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.paymentsOnEmployeesBehalf,
        path = "/employment/benefitsInKind/paymentsOnEmployeesBehalf"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.personalIncidentalExpenses,
        path = "/employment/benefitsInKind/personalIncidentalExpenses"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.qualifyingRelocationExpenses,
        path = "/employment/benefitsInKind/qualifyingRelocationExpenses"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.employerProvidedProfessionalSubscriptions,
        path = "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.employerProvidedServices,
        path = "/employment/benefitsInKind/employerProvidedServices"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.incomeTaxPaidByDirector,
        path = "/employment/benefitsInKind/incomeTaxPaidByDirector"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.travelAndSubsistence,
        path = "/employment/benefitsInKind/travelAndSubsistence"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.vouchersAndCreditCards,
        path = "/employment/benefitsInKind/vouchersAndCreditCards"
      ),
      resolveOptionalNonNegativeNumber(
        amount = benefitsInKind.nonCash,
        path = "/employment/benefitsInKind/nonCash"
      )
    )
  }

  private def validateOffPayrollWorker(taxYear: TaxYear, offPayrollWorker: Option[Boolean]) = {
    if (taxYear.useTaxYearSpecificApi)
      Validated.cond(offPayrollWorker.isDefined, (), Seq(RuleMissingOffPayrollWorker))
    else
      Validated.cond(offPayrollWorker.isEmpty, (), Seq(RuleNotAllowedOffPayrollWorker))
  }

}
