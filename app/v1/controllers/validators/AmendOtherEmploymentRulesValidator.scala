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

import shared.controllers.validators.RulesValidator
import api.controllers.validators.resolvers._
import shared.models.errors._
import cats.data.Validated
import cats.implicits._
import common.errors.{ClassOfSharesAcquiredFormatError, ClassOfSharesAwardedFormatError, CustomerRefFormatError, EmployerNameFormatError, EmployerRefFormatError, RuleLumpSumsError, SchemePlanTypeFormatError}
import shared.models.errors.MtdError
import v1.controllers.validators.resolvers.EmploymentsIncomeValidators._
import v1.controllers.validators.resolvers.{ShareOptionSchemeTypeResolver, SharesAwardedOrReceivedSchemeTypeResolver}
import v1.models.request.amendOtherEmployment._

object AmendOtherEmploymentRulesValidator extends RulesValidator[AmendOtherEmploymentRequest] with ResolverSupport {

  def validateBusinessRules(parsed: AmendOtherEmploymentRequest): Validated[Seq[MtdError], AmendOtherEmploymentRequest] = {
    import parsed.body

    combine(
      validateOptionalSeqWith(body.shareOption)(validateShareOption),
      validateOptionalSeqWith(body.sharesAwardedOrReceived)(validateSharesAwaredOrReceived),
      validateOptionalWith(body.disability)(validateDisability),
      validateOptionalWith(body.foreignService)(validateForeignService),
      validateOptionalSeqWith(body.lumpSums)(validateLumpSums)
    ).onSuccess(parsed)

  }

  private def validateOptionalSeqWith[A](field: Option[Seq[A]])(validator: (A, Int) => Validated[Seq[MtdError], Unit]) =
    field match {
      case Some(values) =>
        values.zipWithIndex.toList.traverse_ { case (value, index) => validator(value, index) }
      case None => valid
    }

  private def validateOptionalWith[A](field: Option[A])(validator: A => Validated[Seq[MtdError], Unit]) =
    field match {
      case Some(value) => validator(value)
      case None        => valid
    }

  private def validateDate(path: String) =
    ResolveIsoDate(DateFormatError.withPath(path)).resolver
      .map(_.getYear)
      .thenValidate(inRange(1900, 2099, RuleDateRangeInvalidError.withPath(path)))

  private def validateShareOption(item: AmendShareOptionItem, arrayIndex: Int) = {

    def validateSchemeType =
      ShareOptionSchemeTypeResolver
        .resolver(SchemePlanTypeFormatError.withPath(s"/shareOption/$arrayIndex/schemePlanType"))(item.schemePlanType)

    combine(
      validateOtherEmployerName(item.employerName, EmployerNameFormatError.withPath(s"/shareOption/$arrayIndex/employerName")),
      validateOptionalEmployerRef(item.employerRef, EmployerRefFormatError.withPath(s"/shareOption/$arrayIndex/employerRef")),
      validateClassOfShares(item.classOfSharesAcquired, ClassOfSharesAcquiredFormatError.withPath(s"/shareOption/$arrayIndex/classOfSharesAcquired")),
      validateSchemeType,
      validateDate(s"/shareOption/$arrayIndex/dateOfOptionGrant")(item.dateOfOptionGrant),
      validateDate(s"/shareOption/$arrayIndex/dateOfEvent")(item.dateOfEvent),
      validatePositiveInt(
        amount = item.noOfSharesAcquired,
        path = s"/shareOption/$arrayIndex/noOfSharesAcquired"
      ),
      validateNonNegativeNumber(
        amount = item.amountOfConsiderationReceived,
        path = s"/shareOption/$arrayIndex/amountOfConsiderationReceived"
      ),
      validateNonNegativeNumber(
        item.exercisePrice,
        s"/shareOption/$arrayIndex/exercisePrice"
      ),
      validateNonNegativeNumber(
        amount = item.amountPaidForOption,
        path = s"/shareOption/$arrayIndex/amountPaidForOption"
      ),
      validateNonNegativeNumber(
        amount = item.marketValueOfSharesOnExcise,
        path = s"/shareOption/$arrayIndex/marketValueOfSharesOnExcise"
      ),
      validateNonNegativeNumber(
        amount = item.profitOnOptionExercised,
        path = s"/shareOption/$arrayIndex/profitOnOptionExercised"
      ),
      validateNonNegativeNumber(
        amount = item.employersNicPaid,
        path = s"/shareOption/$arrayIndex/employersNicPaid"
      ),
      validateNonNegativeNumber(
        amount = item.taxableAmount,
        path = s"/shareOption/$arrayIndex/taxableAmount"
      )
    )
  }

  private def validateSharesAwaredOrReceived(item: AmendSharesAwardedOrReceivedItem, arrayIndex: Int) = {

    def validateSchemeType =
      SharesAwardedOrReceivedSchemeTypeResolver
        .resolver(SchemePlanTypeFormatError.withPath(s"/sharesAwardedOrReceived/$arrayIndex/schemePlanType"))(item.schemePlanType)

    combine(
      validateOtherEmployerName(item.employerName, EmployerNameFormatError.withPath(s"/sharesAwardedOrReceived/$arrayIndex/employerName")),
      validateOptionalEmployerRef(item.employerRef, EmployerRefFormatError.withPath(s"/sharesAwardedOrReceived/$arrayIndex/employerRef")),
      validateClassOfShares(
        item.classOfShareAwarded,
        ClassOfSharesAwardedFormatError.withPath(s"/sharesAwardedOrReceived/$arrayIndex/classOfShareAwarded")),
      validateSchemeType,
      validateDate(s"/sharesAwardedOrReceived/$arrayIndex/dateSharesCeasedToBeSubjectToPlan")(item.dateSharesCeasedToBeSubjectToPlan),
      validateDate(s"/sharesAwardedOrReceived/$arrayIndex/dateSharesAwarded")(item.dateSharesAwarded),
      validatePositiveInt(
        amount = item.noOfShareSecuritiesAwarded,
        path = s"/sharesAwardedOrReceived/$arrayIndex/noOfShareSecuritiesAwarded"
      ),
      validateNonNegativeNumber(
        amount = item.actualMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/actualMarketValueOfSharesOnAward"
      ),
      validateNonNegativeNumber(
        amount = item.unrestrictedMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/unrestrictedMarketValueOfSharesOnAward"
      ),
      validateNonNegativeNumber(
        amount = item.amountPaidForSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/amountPaidForSharesOnAward"
      ),
      validateNonNegativeNumber(
        amount = item.marketValueAfterRestrictionsLifted,
        path = s"/sharesAwardedOrReceived/$arrayIndex/marketValueAfterRestrictionsLifted"
      ),
      validateNonNegativeNumber(
        amount = item.taxableAmount,
        path = s"/sharesAwardedOrReceived/$arrayIndex/taxableAmount"
      )
    )
  }

  private def validateDisability(item: AmendCommonOtherEmployment) =
    validateCommon(item, x => s"/disability/$x")

  private def validateForeignService(item: AmendCommonOtherEmployment) =
    validateCommon(item, x => s"/foreignService/$x")

  private def validateCommon(item: AmendCommonOtherEmployment, path: String => String) = {

    combine(
      validateCustomerRef(item.customerReference, CustomerRefFormatError.withPath(path("customerReference"))),
      validateNonNegativeNumber(
        amount = item.amountDeducted,
        path("amountDeducted")
      )
    )
  }

  private def validateLumpSums(item: AmendLumpSums, arrayIndex: Int) = {

    def validateLumpSumSectionPresent = {
      val validator = (lumpSums: AmendLumpSums) =>
        Option.when(
          Seq(
            lumpSums.taxableLumpSumsAndCertainIncome,
            lumpSums.benefitFromEmployerFinancedRetirementScheme,
            lumpSums.redundancyCompensationPaymentsOverExemption,
            lumpSums.redundancyCompensationPaymentsUnderExemption
          ).forall(_.isEmpty))(Seq(RuleLumpSumsError.withPath(s"/lumpSums/$arrayIndex")))

      resolveValid[AmendLumpSums].thenValidate(validator)(item)
    }

    combine(
      validateOtherEmployerName(item.employerName, EmployerNameFormatError.withPath(s"/lumpSums/$arrayIndex/employerName")),
      validateEmployerRef(item.employerRef, EmployerRefFormatError.withPath(s"/lumpSums/$arrayIndex/employerRef")),
      validateLumpSumSectionPresent,
      validateOptionalNonNegativeNumber(
        amount = item.taxableLumpSumsAndCertainIncome.map(_.amount),
        path = s"/lumpSums/$arrayIndex/taxableLumpSumsAndCertainIncome/amount"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.taxableLumpSumsAndCertainIncome.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/taxableLumpSumsAndCertainIncome/taxPaid"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.benefitFromEmployerFinancedRetirementScheme.map(_.amount),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementScheme/amount"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.benefitFromEmployerFinancedRetirementScheme.flatMap(_.exemptAmount),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementScheme/exemptAmount"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.benefitFromEmployerFinancedRetirementScheme.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/benefitFromEmployerFinancedRetirementScheme/taxPaid"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.redundancyCompensationPaymentsOverExemption.map(_.amount),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsOverExemption/amount"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.redundancyCompensationPaymentsOverExemption.flatMap(_.taxPaid),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsOverExemption/taxPaid"
      ),
      validateOptionalNonNegativeNumber(
        amount = item.redundancyCompensationPaymentsUnderExemption.map(_.amount),
        path = s"/lumpSums/$arrayIndex/redundancyCompensationPaymentsUnderExemption/amount"
      )
    )

  }

}
