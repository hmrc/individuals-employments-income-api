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

import api.controllers.validators.resolvers.ResolverSupport
import shared.models.domain.TaxYear
import shared.models.errors._
import cats.data.Validated.{Invalid, Valid}
import common.errors.{CessationDateFormatError, RuleCessationDateBeforeStartDateError, RuleCessationDateBeforeTaxYearStartError, RuleStartDateAfterTaxYearEndError}
import shared.controllers.validators.resolvers.ResolveIsoDate
import shared.models.errors.MtdError

import java.time.LocalDate
import scala.math.Ordering.Implicits.infixOrderingOps

object CustomEmploymentDateValidator extends ResolverSupport {
  private type StringDateRange = (String, Option[String])

  def validator(taxYear: TaxYear): Validator[StringDateRange] = { case (startDate: String, cessationDate: Option[String]) =>
    def validateStartDate(date: LocalDate) =
      (satisfiesMax(taxYear.endDate, RuleStartDateAfterTaxYearEndError) thenValidate
        isInRange(StartDateFormatError))(date)

    def validateCessationDate(maybeDate: Option[LocalDate]) =
      (satisfiesMin(taxYear.startDate, RuleCessationDateBeforeTaxYearStartError) thenValidate
        isInRange(CessationDateFormatError)).validateOptionally(maybeDate)

    (resolveStartDate(startDate), resolveCessationDate(cessationDate)) match {
      case (Valid(start), Valid(maybeCessation)) =>
        combineErrors(
          validateStartDate(start),
          validateCessationDate(maybeCessation),
          validateDatesInOrder(start, maybeCessation)
        )

      case (Valid(start), Invalid(errs)) =>
        combineErrors(
          Some(errs),
          validateStartDate(start)
        )

      case (Invalid(errs), Valid(maybeCessation)) =>
        combineErrors(
          Some(errs),
          validateCessationDate(maybeCessation)
        )

      case (Invalid(errs1), Invalid(errs2)) => combineErrors(Some(errs1), Some(errs2))
    }
  }

  private def resolveStartDate(startDate: String) =
    ResolveIsoDate(StartDateFormatError).resolver(startDate)

  private def resolveCessationDate(cessationDate: Option[String]) =
    ResolveIsoDate(CessationDateFormatError).resolver.resolveOptionally(cessationDate)

  private def isInRange(error: => MtdError): Validator[LocalDate] =
    inRange(1900, 2099, error).contramap(_.getYear)

  private def validateDatesInOrder(startDate: LocalDate, maybeCessation: Option[LocalDate]) =
    maybeCessation.flatMap { cessationDate =>
      Option.when(cessationDate < startDate)(Seq(RuleCessationDateBeforeStartDateError))
    }

  private def combineErrors(maybeErrs: Option[Seq[MtdError]]*): Option[Seq[MtdError]] =
    maybeErrs
      .collect { case Some(errs) => errs }
      .reduceOption(_ ++ _)

}
