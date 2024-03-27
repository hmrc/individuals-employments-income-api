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
import api.models.errors.MtdError
import config.AppConfig
import v1.controllers.requestParsers.validators.validation.{DateFormatValidation, LumpSumsRuleValidation}
import v1.models.request.amendOtherEmployment._

import javax.inject.{Inject, Singleton}

@Singleton
class AmendOtherEmploymentValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[AmendOtherEmploymentRawData]
    with ValueFormatErrorMessages {

  private val validationSet = List(
    parameterFormatValidation,
    parameterRuleValidation,
    bodyFormatValidator,
    bodyValueValidator,
    bodyRuleValidator
  )

  override def validate(data: AmendOtherEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendOtherEmploymentRawData => List[List[MtdError]] = (data: AmendOtherEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear)
    )
  }

  private def bodyFormatValidator: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendOtherEmploymentRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[AmendOtherEmploymentRequestBody]

    List(
      flattenErrors(
        List(
          requestBodyData.shareOption
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateShareOption(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.sharesAwardedOrReceived
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateSharesAwardedOrReceivedItem(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList,
          requestBodyData.disability.map { data => validateCommonOtherEmployment(data, fieldName = "disability") }.getOrElse(NoValidationErrors),
          requestBodyData.foreignService
            .map { data => validateCommonOtherEmployment(data, fieldName = "foreignService") }
            .getOrElse(NoValidationErrors),
          requestBodyData.lumpSums
            .map(_.zipWithIndex.flatMap { case (data, index) =>
              validateLumpSums(data, index)
            })
            .getOrElse(NoValidationErrors)
            .toList
        )
      ))
  }

  private def validateShareOption(shareOptionItem: AmendShareOptionItem, arrayIndex: Int): List[MtdError] = {
    List(
      EmployerNameValidation
        .validateOtherEmployment(shareOptionItem.employerName)
        .map(
          _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/employerName")))
        ),
      EmployerRefValidation
        .validateOptional(shareOptionItem.employerRef)
        .map(
          _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/employerRef")))
        ),
      SchemePlanTypeValidation
        .validate(shareOptionItem.schemePlanType.toString, awarded = false)
        .map(
          _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/schemePlanType")))
        ),
      DateFormatValidation.validateOptional(
        date = Some(shareOptionItem.dateOfOptionGrant),
        path = Some(s"/shareOption/$arrayIndex/dateOfOptionGrant")
      ),
      DateFormatValidation.validateOptional(
        date = Some(shareOptionItem.dateOfEvent),
        path = Some(s"/shareOption/$arrayIndex/dateOfEvent")
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.amountOfConsiderationReceived,
        path = s"/shareOption/$arrayIndex/amountOfConsiderationReceived"
      ),
      BigIntegerValueValidation.validate(
        field = shareOptionItem.noOfSharesAcquired,
        path = s"/shareOption/$arrayIndex/noOfSharesAcquired"
      ),
      ClassOfSharesValidation
        .validate(shareOptionItem.classOfSharesAcquired, acquired = true)
        .map(
          _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/classOfSharesAcquired")))
        ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.exercisePrice,
        path = s"/shareOption/$arrayIndex/exercisePrice"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.amountPaidForOption,
        path = s"/shareOption/$arrayIndex/amountPaidForOption"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.marketValueOfSharesOnExcise,
        path = s"/shareOption/$arrayIndex/marketValueOfSharesOnExcise"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.profitOnOptionExercised,
        path = s"/shareOption/$arrayIndex/profitOnOptionExercised"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.employersNicPaid,
        path = s"/shareOption/$arrayIndex/employersNicPaid"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.taxableAmount,
        path = s"/shareOption/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateSharesAwardedOrReceivedItem(sharesAwardedOrReceivedItem: AmendSharesAwardedOrReceivedItem, arrayIndex: Int): List[MtdError] = {
    List(
      EmployerNameValidation
        .validateOtherEmployment(sharesAwardedOrReceivedItem.employerName)
        .map(
          _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/employerName")))
        ),
      EmployerRefValidation
        .validateOptional(sharesAwardedOrReceivedItem.employerRef)
        .map(
          _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/employerRef")))
        ),
      SchemePlanTypeValidation
        .validate(sharesAwardedOrReceivedItem.schemePlanType.toString, awarded = true)
        .map(
          _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/schemePlanType")))
        ),
      DateFormatValidation.validateOptional(
        date = Some(sharesAwardedOrReceivedItem.dateSharesCeasedToBeSubjectToPlan),
        path = Some(s"/sharesAwardedOrReceived/$arrayIndex/dateSharesCeasedToBeSubjectToPlan")
      ),
      BigIntegerValueValidation.validate(
        field = sharesAwardedOrReceivedItem.noOfShareSecuritiesAwarded,
        path = s"/sharesAwardedOrReceived/$arrayIndex/noOfShareSecuritiesAwarded"
      ),
      ClassOfSharesValidation
        .validate(sharesAwardedOrReceivedItem.classOfShareAwarded, acquired = false)
        .map(
          _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/classOfShareAwarded")))
        ),
      DateFormatValidation.validateOptional(
        date = Some(sharesAwardedOrReceivedItem.dateSharesAwarded),
        path = Some(s"/sharesAwardedOrReceived/$arrayIndex/dateSharesAwarded")
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.actualMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/actualMarketValueOfSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.unrestrictedMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/unrestrictedMarketValueOfSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.amountPaidForSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/amountPaidForSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.marketValueAfterRestrictionsLifted,
        path = s"/sharesAwardedOrReceived/$arrayIndex/marketValueAfterRestrictionsLifted"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.taxableAmount,
        path = s"/sharesAwardedOrReceived/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateCommonOtherEmployment(commonOtherEmployment: AmendCommonOtherEmployment, fieldName: String): List[MtdError] = {
    List(
      CustomerRefValidation
        .validateOptional(commonOtherEmployment.customerReference)
        .map(
          _.copy(paths = Some(Seq(s"/$fieldName/customerReference")))
        ),
      DecimalValueValidation.validate(
        amount = commonOtherEmployment.amountDeducted,
        path = s"/$fieldName/amountDeducted"
      )
    ).flatten
  }

  private def validateLumpSums(lumpSums: AmendLumpSums, arrayIndex: Int): List[MtdError] = {
    List(
      EmployerNameValidation
        .validateOtherEmployment(lumpSums.employerName)
        .map(
          _.copy(paths = Some(Seq(s"/lumpSums/$arrayIndex/employerName")))
        ),
      EmployerRefValidation
        .validate(lumpSums.employerRef)
        .map(
          _.copy(paths = Some(Seq(s"/lumpSums/$arrayIndex/employerRef")))
        ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.taxableLumpSumsAndCertainIncome.map(_.amount),
        path = s"/lumpSums/$arrayIndex/taxableLumpSumsAndCertainIncome/amount"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.taxableLumpSumsAndCertainIncome.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/taxableLumpSumsAndCertainIncome/taxPaid"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.benefitFromEmployerFinancedRetirementScheme.map(_.amount),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementSchemeItem/amount"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.benefitFromEmployerFinancedRetirementScheme.flatMap(_.exemptAmount),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementSchemeItem/exemptAmount"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.benefitFromEmployerFinancedRetirementScheme.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementSchemeItem/taxPaid"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.redundancyCompensationPaymentsOverExemption.map(_.amount),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsOverExemptionItem/amount"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.redundancyCompensationPaymentsOverExemption.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsOverExemptionItem/taxPaid"
      ),
      DecimalValueValidation.validateOptional(
        amount = lumpSums.redundancyCompensationPaymentsUnderExemption.map(_.amount),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsUnderExemptionItem/amount"
      )
    ).flatten
  }

  private def bodyRuleValidator: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[AmendOtherEmploymentRequestBody]

    List(
      flattenErrors(
        requestBodyData.lumpSums.fold[List[List[MtdError]]](NoValidationErrors) { lumpSums =>
          lumpSums.zipWithIndex.map(indexedLumpSums => LumpSumsRuleValidation.validate(indexedLumpSums._1, indexedLumpSums._2)).toList
        }
      ))
  }

}
