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

import shared.models.domain.TaxYear
import shared.models.errors._
import shared.utils.UnitSpec
import v1.controllers.validators.resolvers.CustomEmploymentDateValidator.validator

class CustomEmploymentDateValidatorSpec extends UnitSpec {

  "CustomEmploymentDateValidator" should {

    def validate(startDate: String, cessationDate: Option[String], taxYear: TaxYear) =
      validator(taxYear)((startDate, cessationDate))

    "return None when only start date is supplied and it is valid" in {
      validate(
        startDate = "2020-07-11",
        cessationDate = None,
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe None
    }

    "return None when both dates are supplied and are valid" in {
      validate(
        startDate = "2020-07-11",
        cessationDate = Some("2020-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe None
    }

    "return multiple format errors when both dates are supplied with the incorrect format" in {
      validate(
        startDate = "2020-07-111",
        cessationDate = Some("2020-07-121"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError, CessationDateFormatError))
    }

    "return multiple format errors when both dates are supplied with the dates outside of allowed range" in {
      validate(
        startDate = "0010-07-11",
        cessationDate = Some("2120-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError, CessationDateFormatError))
    }

    "return only a CessationDateFormatError when cessation date format is incorrect and start date is valid" in {
      validate(
        startDate = "2020-07-11",
        cessationDate = Some("2020-07-121"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(CessationDateFormatError))
    }

    "return only a CessationDateFormatError when cessation date is outside of allowed range and start date is valid" in {
      validate(
        startDate = "2020-07-11",
        cessationDate = Some("2120-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(CessationDateFormatError))
    }

    "return only a StartDateFormatError when start date format is incorrect and cessation date is valid" in {
      validate(
        startDate = "2020-07-111",
        cessationDate = Some("2020-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError))
    }

    "return only a StartDateFormatError when start date is outside of allowed range and cessation date is valid" in {
      validate(
        startDate = "0010-01-01",
        cessationDate = Some("2020-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError))
    }

    "return only a StartDateFormatError when start date format is incorrect and cessation date is not provided" in {
      validate(
        startDate = "2020-07-111",
        cessationDate = None,
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError))
    }

    // mixed error scenarios
    "return multiple errors when cessation date format is invalid and start date exceeds the tax year" in {
      validate(
        startDate = "2022-07-11",
        cessationDate = Some("2022-07-121"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(CessationDateFormatError, RuleStartDateAfterTaxYearEndError))
    }

    "return multiple errors when start date format is invalid and cessation date predates the tax year" in {
      validate(
        startDate = "2020-07-111",
        cessationDate = Some("2019-07-12"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(StartDateFormatError, RuleCessationDateBeforeTaxYearStartError))
    }

    "return RuleStartDateAfterTaxYearEndError when cessation date is not provided and start date exceeds the tax year" in {
      validate(
        startDate = "2022-07-11",
        cessationDate = None,
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(RuleStartDateAfterTaxYearEndError))
    }

    "return multiple errors when start date exceeds both tax year and cessation date" in {
      validate(
        startDate = "2022-07-11",
        cessationDate = Some("2022-07-10"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(RuleStartDateAfterTaxYearEndError, RuleCessationDateBeforeStartDateError))
    }

    "return multiple errors when cessation date predates both tax year and start date" in {
      validate(
        startDate = "2019-07-11",
        cessationDate = Some("2019-07-10"),
        taxYear = TaxYear.fromMtd("2020-21")
      ) shouldBe Some(Seq(RuleCessationDateBeforeTaxYearStartError, RuleCessationDateBeforeStartDateError))
    }
  }

}
