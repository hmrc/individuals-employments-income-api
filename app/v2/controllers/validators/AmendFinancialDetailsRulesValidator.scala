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
import common.errors.{RuleMissingOffPayrollWorker, RuleNotAllowedOffPayrollWorker}
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v2.controllers.validators.resolvers.EmploymentsIncomeValidators._
import v2.models.request.amendFinancialDetails.AmendFinancialDetailsRequest
import v2.models.request.amendFinancialDetails.employment.{AmendBenefitsInKind, AmendDeductions, AmendPay}

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

  private def validatePay(pay: AmendPay) = {
    combine(
      validateNonNegativeNumber(
        amount = pay.taxablePayToDate,
        path = "/employment/pay/taxablePayToDate"
      ),
      validateNumber(
        amount = pay.totalTaxToDate,
        path = "/employment/pay/totalTaxToDate"
      )
    )
  }

  private def validateDeductions(deductions: AmendDeductions) = {
    combine(
      validateOptionalNonNegativeNumber(
        amount = deductions.studentLoans.flatMap(_.uglDeductionAmount),
        path = "/employment/deductions/studentLoans/uglDeductionAmount"
      ),
      validateOptionalNonNegativeNumber(
        amount = deductions.studentLoans.flatMap(_.pglDeductionAmount),
        path = "/employment/deductions/studentLoans/pglDeductionAmount"
      )
    )
  }

  private def validateBenefitsInKind(benefitsInKind: AmendBenefitsInKind) = {
    combine(
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.accommodation,
        path = "/employment/benefitsInKind/accommodation"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.assets,
        path = "/employment/benefitsInKind/assets"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.assetTransfer,
        path = "/employment/benefitsInKind/assetTransfer"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.beneficialLoan,
        path = "/employment/benefitsInKind/beneficialLoan"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.car,
        path = "/employment/benefitsInKind/car"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.carFuel,
        path = "/employment/benefitsInKind/carFuel"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.educationalServices,
        path = "/employment/benefitsInKind/educationalServices"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.entertaining,
        path = "/employment/benefitsInKind/entertaining"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.expenses,
        path = "/employment/benefitsInKind/expenses"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.medicalInsurance,
        path = "/employment/benefitsInKind/medicalInsurance"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.telephone,
        path = "/employment/benefitsInKind/telephone"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.service,
        path = "/employment/benefitsInKind/service"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.taxableExpenses,
        path = "/employment/benefitsInKind/taxableExpenses"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.van,
        path = "/employment/benefitsInKind/van"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.vanFuel,
        path = "/employment/benefitsInKind/vanFuel"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.mileage,
        path = "/employment/benefitsInKind/mileage"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.nonQualifyingRelocationExpenses,
        path = "/employment/benefitsInKind/nonQualifyingRelocationExpenses"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.nurseryPlaces,
        path = "/employment/benefitsInKind/nurseryPlaces"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.otherItems,
        path = "/employment/benefitsInKind/otherItems"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.paymentsOnEmployeesBehalf,
        path = "/employment/benefitsInKind/paymentsOnEmployeesBehalf"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.personalIncidentalExpenses,
        path = "/employment/benefitsInKind/personalIncidentalExpenses"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.qualifyingRelocationExpenses,
        path = "/employment/benefitsInKind/qualifyingRelocationExpenses"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.employerProvidedProfessionalSubscriptions,
        path = "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.employerProvidedServices,
        path = "/employment/benefitsInKind/employerProvidedServices"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.incomeTaxPaidByDirector,
        path = "/employment/benefitsInKind/incomeTaxPaidByDirector"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.travelAndSubsistence,
        path = "/employment/benefitsInKind/travelAndSubsistence"
      ),
      validateOptionalNonNegativeNumber(
        amount = benefitsInKind.vouchersAndCreditCards,
        path = "/employment/benefitsInKind/vouchersAndCreditCards"
      ),
      validateOptionalNonNegativeNumber(
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
