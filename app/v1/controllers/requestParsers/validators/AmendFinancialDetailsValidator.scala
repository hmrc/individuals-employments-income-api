/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError, RuleMissingOffPayrollWorker, RuleNotAllowedOffPayrollWorker}
import config.AppConfig
import v1.models.request.amendFinancialDetails.emploment.AmendEmployment
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRawData, AmendFinancialDetailsRequestBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendFinancialDetailsValidator @Inject() (implicit appConfig: AppConfig)
  extends Validator[AmendFinancialDetailsRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendFinancialDetailsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendFinancialDetailsRawData => List[List[MtdError]] = (data: AmendFinancialDetailsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      EmploymentIdValidation.validate(data.employmentId)
    )
  }

  private def parameterRuleValidation: AmendFinancialDetailsRawData => List[List[MtdError]] = (data: AmendFinancialDetailsRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: AmendFinancialDetailsRawData => List[List[MtdError]] = { data =>
    val jsonFormatError = List(
      JsonFormatValidation.validate[AmendFinancialDetailsRequestBody](data.body.json)
    )

    val requestBodyObj = data.body.json.asOpt[AmendFinancialDetailsRequestBody]
    val emptyObjectValidation: List[List[MtdError]] = List(
      requestBodyObj
        .map { body =>
          val emptyDeductionsError: List[String] =
            if (body.employment.deductions.exists(_.isEmpty)) List("/employment/deductions") else NoValidationErrors

          val emptyBenefitsInKindError: List[String] =
            if (body.employment.benefitsInKind.exists(_.isEmpty)) List("/employment/benefitsInKind") else NoValidationErrors

          val emptyStudentLoansError: List[String] =
            if (body.employment.deductions.exists(_.studentLoans.exists(_.isEmpty))) List("/employment/deductions/studentLoans")
            else NoValidationErrors

          val minimumFieldsError = emptyDeductionsError ++ emptyBenefitsInKindError ++ emptyStudentLoansError
          if (minimumFieldsError.isEmpty) NoValidationErrors else List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(minimumFieldsError)))

        }
        .getOrElse(NoValidationErrors))

    def offPayrollWorkerAllowedValidation(data: AmendFinancialDetailsRawData): List[List[MtdError]] = {

      if (TaxYearValidation.validate(data.taxYear) == NoValidationErrors) {
        val isTysTaxYear                           = (TaxYear.fromMtd(data.taxYear).year >= 2024) & data.opwEnabled
        val maybeOffPayrollWorker: Option[Boolean] = requestBodyObj.flatMap(entity => entity.employment.offPayrollWorker)
        maybeOffPayrollWorker match {
          case None if isTysTaxYear     => List(List(RuleMissingOffPayrollWorker))
          case Some(_) if !isTysTaxYear => List(List(RuleNotAllowedOffPayrollWorker))
          case _                        => NoValidationErrors
        }
      } else {
        NoValidationErrors
      }
    }

    val initialValidation = jsonFormatError ++ emptyObjectValidation

    if (initialValidation.flatten.isEmpty) {
      initialValidation ++ offPayrollWorkerAllowedValidation(data)
    } else {
      initialValidation
    }
  }

  private def bodyValueValidator: AmendFinancialDetailsRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[AmendFinancialDetailsRequestBody]

    List(
      flattenErrors(
        List(
          validateEmployment(requestBodyData.employment)
        )
      ))
  }

  private def validateEmployment(employment: AmendEmployment): List[MtdError] = {
    List(
      DecimalValueValidation.validate(
        amount = employment.pay.taxablePayToDate,
        path = "/employment/pay/taxablePayToDate"
      ),
      DecimalValueValidation.validate(
        amount = employment.pay.totalTaxToDate,
        path = "/employment/pay/totalTaxToDate",
        minValue = -99999999999.99,
        message = BIG_DECIMAL_MINIMUM_INCLUSIVE
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.deductions.flatMap(_.studentLoans.flatMap(_.uglDeductionAmount)),
        path = "/employment/deductions/studentLoans/uglDeductionAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.deductions.flatMap(_.studentLoans.flatMap(_.pglDeductionAmount)),
        path = "/employment/deductions/studentLoans/pglDeductionAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.accommodation),
        path = "/employment/benefitsInKind/accommodation"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.assets),
        path = "/employment/benefitsInKind/assets"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.assetTransfer),
        path = "/employment/benefitsInKind/assetTransfer"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.beneficialLoan),
        path = "/employment/benefitsInKind/beneficialLoan"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.car),
        path = "/employment/benefitsInKind/car"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.carFuel),
        path = "/employment/benefitsInKind/carFuel"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.educationalServices),
        path = "/employment/benefitsInKind/educationalServices"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.entertaining),
        path = "/employment/benefitsInKind/entertaining"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.expenses),
        path = "/employment/benefitsInKind/expenses"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.medicalInsurance),
        path = "/employment/benefitsInKind/medicalInsurance"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.telephone),
        path = "/employment/benefitsInKind/telephone"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.service),
        path = "/employment/benefitsInKind/service"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.taxableExpenses),
        path = "/employment/benefitsInKind/taxableExpenses"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.van),
        path = "/employment/benefitsInKind/van"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.vanFuel),
        path = "/employment/benefitsInKind/vanFuel"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.mileage),
        path = "/employment/benefitsInKind/mileage"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.nonQualifyingRelocationExpenses),
        path = "/employment/benefitsInKind/nonQualifyingRelocationExpenses"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.nurseryPlaces),
        path = "/employment/benefitsInKind/nurseryPlaces"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.otherItems),
        path = "/employment/benefitsInKind/otherItems"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.paymentsOnEmployeesBehalf),
        path = "/employment/benefitsInKind/paymentsOnEmployeesBehalf"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.personalIncidentalExpenses),
        path = "/employment/benefitsInKind/personalIncidentalExpenses"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.qualifyingRelocationExpenses),
        path = "/employment/benefitsInKind/qualifyingRelocationExpenses"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.employerProvidedProfessionalSubscriptions),
        path = "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.employerProvidedServices),
        path = "/employment/benefitsInKind/employerProvidedServices"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.incomeTaxPaidByDirector),
        path = "/employment/benefitsInKind/incomeTaxPaidByDirector"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.travelAndSubsistence),
        path = "/employment/benefitsInKind/travelAndSubsistence"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.vouchersAndCreditCards),
        path = "/employment/benefitsInKind/vouchersAndCreditCards"
      ),
      DecimalValueValidation.validateOptional(
        amount = employment.benefitsInKind.flatMap(_.nonCash),
        path = "/employment/benefitsInKind/nonCash"
      )
    ).flatten
  }

}
