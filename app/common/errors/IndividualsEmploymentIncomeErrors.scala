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

package common.errors

import play.api.http.Status._
import shared.models.errors.MtdError

object EmploymentIdFormatError    extends MtdError("FORMAT_EMPLOYMENT_ID", "The provided employment ID is invalid", BAD_REQUEST)
object CustomerRefFormatError extends MtdError("FORMAT_CUSTOMER_REF", "The provided customer reference is invalid", BAD_REQUEST)
object EmployerNameFormatError extends MtdError("FORMAT_EMPLOYER_NAME", "The provided employer name is invalid", BAD_REQUEST)
object EmployerRefFormatError  extends MtdError("FORMAT_EMPLOYER_REF", "The provided employer ref is invalid", BAD_REQUEST)
object DateFormatError         extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD", BAD_REQUEST)

object ClassOfSharesAwardedFormatError
    extends MtdError("FORMAT_CLASS_OF_SHARES_AWARDED", "The provided class of shares awarded is invalid", BAD_REQUEST)

object ClassOfSharesAcquiredFormatError
    extends MtdError("FORMAT_CLASS_OF_SHARES_ACQUIRED", "The provided class of shares acquired is invalid", BAD_REQUEST)

object SchemePlanTypeFormatError   extends MtdError("FORMAT_SCHEME_PLAN_TYPE", "The provided scheme plan type is invalid", BAD_REQUEST)
object PayrollIdFormatError        extends MtdError("FORMAT_PAYROLL_ID", "The provided payroll ID is invalid", BAD_REQUEST)
object CessationDateFormatError    extends MtdError("FORMAT_CESSATION_DATE", "The provided cessation date is invalid", BAD_REQUEST)
object SourceFormatError           extends MtdError("FORMAT_SOURCE", "The provided source is invalid", BAD_REQUEST)

// Rule Errors

object RuleInvalidSubmissionPensionSchemeError
    extends MtdError(
      "RULE_INVALID_SUBMISSION_PENSION_SCHEME",
      "Data item submitted is invalid for a customer with an occupational pension and no other employment source - for example, employment expenses or benefit in kind (apart from medical insurance) are not valid.",
      BAD_REQUEST)

object RuleCessationDateBeforeStartDateError
    extends MtdError("RULE_CESSATION_DATE_BEFORE_START_DATE", "The cessation date cannot be earlier than the start date", BAD_REQUEST)

object RuleStartDateAfterTaxYearEndError
    extends MtdError("RULE_START_DATE_AFTER_TAX_YEAR_END", "The start date cannot be later than the tax year end", BAD_REQUEST)

object RuleCessationDateBeforeTaxYearStartError
    extends MtdError("RULE_CESSATION_DATE_BEFORE_TAX_YEAR_START", "The cessation date cannot be before the tax year starts", BAD_REQUEST)

object RuleUpdateForbiddenError extends MtdError("RULE_UPDATE_FORBIDDEN", "The update for an HMRC held employment is not permitted", BAD_REQUEST)

object RuleCustomEmploymentError extends MtdError("RULE_CUSTOM_EMPLOYMENT", "A custom employment cannot be ignored", BAD_REQUEST)

object RuleCustomEmploymentUnignoreError extends MtdError("RULE_CUSTOM_EMPLOYMENT", "A custom employment cannot be unignored", BAD_REQUEST)

object RuleLumpSumsError extends MtdError("RULE_LUMP_SUMS", "At least one child object is required when lumpSums are provided", BAD_REQUEST)

object RuleDeleteForbiddenError extends MtdError("RULE_DELETE_FORBIDDEN", "A deletion of an HMRC held employment is not permitted", BAD_REQUEST)

object RuleNotAllowedOffPayrollWorker
    extends MtdError("NOT_ALLOWED_OFF_PAYROLL_WORKER", "offPayrollWorker field is not allowed for tax years prior to 2023-24", BAD_REQUEST)

object RuleMissingOffPayrollWorker extends MtdError("MISSING_OFF_PAYROLL_WORKER", "The offPayrollWorker field was not provided", BAD_REQUEST)

object RuleRequestCannotBeFulfilled
  extends MtdError("RULE_REQUEST_CANNOT_BE_FULFILLED", "Custom (will vary in production depending on the actual error)", 422)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

  def forPathAndMin(path: String, min: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be $min or more")

}

