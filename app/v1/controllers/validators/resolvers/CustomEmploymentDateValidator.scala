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

import api.controllers.validators.resolvers.{ResolveIsoDate, ResolverSupport}
import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated.{Invalid, Valid}

import java.time.LocalDate

object CustomEmploymentDateValidator extends ResolverSupport {
  private type StringDateRange = (String, Option[String])
  private type LocalDateRange  = (LocalDate, Option[LocalDate])

  def validator(taxYear: TaxYear): Validator[StringDateRange] = { case (startDate: String, cessationDate: Option[String]) =>
    val startValidated     = ResolveIsoDate(StartDateFormatError).resolver(startDate)
    val cessationValidated = ResolveIsoDate(CessationDateFormatError).resolver.resolveOptionally(cessationDate)

    (startValidated, cessationValidated) match {
      case (Valid(start), Valid(maybeCessation)) =>
        combineErrors(
          startDateValidator(taxYear)(start),
          cessationDateValidator(taxYear)(maybeCessation),
          checkDateOrder((start, maybeCessation))
        )

      case (Valid(start), Invalid(errs)) =>
        combineErrors(
          Some(errs),
          startDateValidator(taxYear)(start)
        )

      case (Invalid(errs), Valid(maybeCessation)) =>
        combineErrors(
          Some(errs),
          cessationDateValidator(taxYear)(maybeCessation)
        )

      case (Invalid(errs1), Invalid(errs2)) => combineErrors(Some(errs1), Some(errs2))
    }
  }

  private def combineErrors(maybeErrs: Option[Seq[MtdError]]*): Option[Seq[MtdError]] =
    maybeErrs
      .collect { case Some(errs) => errs }
      .reduceOption(_ ++ _)

  private def startDateValidator(taxYear: TaxYear): Validator[LocalDate] =
    satisfiesMax(taxYear.endDate, RuleStartDateAfterTaxYearEndError) thenValidate
      isInRange(StartDateFormatError)

  private def cessationDateValidator(taxYear: TaxYear): Validator[Option[LocalDate]] =
    (satisfiesMin(taxYear.startDate, RuleCessationDateBeforeTaxYearStartError) thenValidate
      isInRange(CessationDateFormatError)).validateOptionally

  private def isInRange(error: => MtdError): Validator[LocalDate] =
    inRange(1900, 2099, error).contramap(_.getYear)


  private val checkDateOrder: Validator[LocalDateRange] = { case (startDate: LocalDate, maybeCessation: Option[LocalDate]) =>
    maybeCessation.flatMap { cessation =>
      Option.when(startDate.isAfter(cessation))(Seq(RuleCessationDateBeforeStartDateError))
    }
  }
}
