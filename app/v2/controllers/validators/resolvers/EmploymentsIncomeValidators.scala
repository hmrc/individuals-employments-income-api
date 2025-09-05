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

package v2.controllers.validators.resolvers

import cats.data.Validated
import common.errors.{CustomerRefFormatError, EmployerNameFormatError, EmployerRefFormatError, PayrollIdFormatError}
import shared.controllers.validators.resolvers.{ResolveParsedNumber, ResolveStringPattern, ResolverSupport}
import shared.models.errors.{MtdError, ValueFormatError}

object EmploymentsIncomeValidators extends ResolverSupport {

  private val otherEmployerNameRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,105}$".r

  def validateOtherEmployerName(name: String, error: => MtdError = EmployerNameFormatError): Validated[Seq[MtdError], String] =
    ResolveStringPattern(otherEmployerNameRegex, error)(name)

  private val customEmployerNameRegex = s"^\\S.{0,73}$$".r

  def validateCustomEmployerName(name: String, error: => MtdError = EmployerNameFormatError): Validated[Seq[MtdError], String] =
    ResolveStringPattern(customEmployerNameRegex, error)(name)

  private val employerRefValidation = "^[0-9]{3}/[^ ].{0,9}$".r

  def validateEmployerRef(value: String, error: => MtdError = EmployerRefFormatError): Validated[Seq[MtdError], String] =
    ResolveStringPattern(employerRefValidation, error)(value)

  def validateOptionalEmployerRef(value: Option[String], error: => MtdError = EmployerRefFormatError): Validated[Seq[MtdError], Option[String]] =
    ResolveStringPattern(employerRefValidation, error).resolver.resolveOptionally(value)

  private val classOfSharesRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def validateClassOfShares(value: String, error: => MtdError): Validated[Seq[MtdError], String] =
    ResolveStringPattern(classOfSharesRegex, error)(value)

  def optionalValidateClassOfShares(value: Option[String], error: => MtdError): Validated[Seq[MtdError], Option[String]] =
    ResolveStringPattern(classOfSharesRegex, error).resolver.resolveOptionally(value)

  private val customerReferenceRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  def validateCustomerRef(value: Option[String], error: => MtdError = CustomerRefFormatError): Validated[Seq[MtdError], Option[String]] =
    ResolveStringPattern(customerReferenceRegex, error).resolver.resolveOptionally(value)

  private val payrollIdRegex ="^[A-Za-z0-9 .,\\-()/=!\"%&*;<>'+:?#]{0,38}$".r

  def validatePayrollId(value: Option[String], error: => MtdError = PayrollIdFormatError): Validated[Seq[MtdError], Option[String]] =
    ResolveStringPattern(payrollIdRegex, error).resolver.resolveOptionally(value)

  def validateOptionalNonNegativeNumber(amount: Option[BigDecimal], path: String): Validated[Seq[MtdError], Option[BigDecimal]] =
    ResolveParsedNumber()(amount, path)

  def validateNonNegativeNumber(amount: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    ResolveParsedNumber()(amount, path)

  def validateNumber(amount: BigDecimal, path: String): Validated[Seq[MtdError], BigDecimal] =
    ResolveParsedNumber(min = -99999999999.99)(amount, path)

  def validatePositiveInt(amount: BigInt, path: String): Validated[Seq[MtdError], BigInt] =
    resolveValid[BigInt].thenValidate(satisfiesMin(0, ValueFormatError.forPathAndMin(path, "0")))(amount)

}
