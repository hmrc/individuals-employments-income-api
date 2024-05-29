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

import api.mocks.MockCurrentDateTime
import api.models.domain.TaxYear
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig

import java.time.LocalDate
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.otherEmploymentIncome.OtherEmploymentIncomeRequestRawData

class OtherEmploymentIncomeRequestValidatorSpec extends UnitSpec with MockAppConfig {

  object Data {
    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"
  }

  import Data._

  class Test extends MockAppConfig with MockCurrentDateTime {

    implicit val appConfig: AppConfig              = mockAppConfig
    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime

    val validator = new OtherEmploymentIncomeRequestValidator()

    MockCurrentDateTime.getLocalDate
      .returns(LocalDate.parse("2021-07-29"))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2017-18")
  }

  "running validation" should {
    "return no errors" when {
      "passed a valid raw request model" in new Test {
        validator.validate(OtherEmploymentIncomeRequestRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "passed an invalid nino" in new Test {
        validator.validate(OtherEmploymentIncomeRequestRawData("A12344A", validTaxYear)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "passed an invalid taxYear" in new Test {
        validator.validate(OtherEmploymentIncomeRequestRawData(validNino, "201920")) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(OtherEmploymentIncomeRequestRawData(validNino, "2016-17")) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(OtherEmploymentIncomeRequestRawData(validNino, "2019-23")) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

  }

}
