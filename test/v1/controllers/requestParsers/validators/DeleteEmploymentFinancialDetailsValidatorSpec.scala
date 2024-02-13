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
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.deleteEmploymentFinancialDetails.DeleteEmploymentFinancialDetailsRawData

class DeleteEmploymentFinancialDetailsValidatorSpec extends UnitSpec {

  private val validNino         = "AA123456A"
  private val validTaxYear      = "2021-22"
  private val validEmploymentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new DeleteEmploymentFinancialDetailsValidator()

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData("A12344A", validTaxYear, validEmploymentId)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData(validNino, "20178", validEmploymentId)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a tax year that is not supported is supplied" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData(validNino, "2018-19", validEmploymentId)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an out of range tax year is supplied" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData(validNino, "2020-22", validEmploymentId)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return NinoFormatError and TaxYearFormatError errors" when {
      "request supplied has invalid nino and tax year" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData("A12344A", "20178", validEmploymentId)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return NinoFormatError, TaxYearFormatError and EmploymentIdFormatError errors" when {
      "request supplied has invalid nino, tax year and employmentId" in new Test {
        validator.validate(DeleteEmploymentFinancialDetailsRawData("A12344A", "20178", "ABCDE12345FG")) shouldBe
          List(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError)
      }
    }
  }

}
