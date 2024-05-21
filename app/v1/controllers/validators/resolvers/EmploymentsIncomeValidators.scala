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

package v1.controllers.validators.resolvers

import api.controllers.validators.resolvers.ResolveStringPattern
import api.models.errors.{CustomerRefFormatError, EmployerNameFormatError, EmployerRefFormatError, MtdError, PayrollIdFormatError}
import v1.controllers.validators.AmendOtherEmploymentRulesValidator.Validator

object EmploymentsIncomeValidators {
  private val employmentNameRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,105}$".r

  def employmentNameValidator(error: => MtdError = EmployerNameFormatError): Validator[String] =
    ResolveStringPattern(employmentNameRegex, error).validator

  private val employerRefValidation = "^[0-9]{3}/[^ ].{0,9}$".r

  def employmentRefValidator(error: => MtdError = EmployerRefFormatError): Validator[String] =
    ResolveStringPattern(employerRefValidation, error).validator

  private val classOfSharesRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def classOfSharesValidator(error: => MtdError): Validator[String] =
    ResolveStringPattern(classOfSharesRegex, error).validator

  private val customerReferenceRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def customerReferenceValidator(error: => MtdError = CustomerRefFormatError): Validator[String] =
    ResolveStringPattern(customerReferenceRegex, error).validator

  private val payrollIdRegex = "^[A-Za-z0-9.,\\-()/=!\"%&*; <>'+:\\?]{0,38}$".r

  def payrollIdValidator(error: => MtdError = PayrollIdFormatError): Validator[String] =
    ResolveStringPattern(payrollIdRegex, error).validator

}
