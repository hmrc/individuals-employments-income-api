/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.controllers.validators

import common.errors._
import common.models.domain.EmploymentId
import config.MockEmploymentsAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v2.models.request.deleteStudentLoansBIK.DeleteStudentLoansBIKRequest

class DeleteStudentLoansBIKValidatorSpec extends UnitSpec with MockEmploymentsAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2024-25"
  private val validEmploymentId              = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val parsedNino         = Nino(validNino)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedEmploymentId = EmploymentId(validEmploymentId)

  trait Test {

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 employmentId: String = validEmploymentId): Either[ErrorWrapper, DeleteStudentLoansBIKRequest] =
      new DeleteStudentLoansBIKValidator(nino, taxYear, employmentId, mockEmploymentsConfig).validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedEmploymentsAppConfig.studentLoansMinimumPermittedTaxYear returns TaxYear.fromMtd("2024-25")
  }

  "validate" should {
    "return a request object" when {
      "valid" in new Test {
        validate() shouldBe Right(DeleteStudentLoansBIKRequest(parsedNino, parsedTaxYear, parsedEmploymentId))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validate(nino = "BAD_NINO") shouldBe singleError(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validate(taxYear = "BAD_TAX_YEAR") shouldBe singleError(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new Test {
        validate(taxYear = "2024-26") shouldBe singleError(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in new Test {
        validate(taxYear = "2023-24") shouldBe singleError(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearNotEndedError error" when {
      "an unsupported tax year is supplied" in new Test {
        validate(taxYear = "2025-26") shouldBe singleError(RuleTaxYearNotEndedError)
      }
    }

    "return EmploymentIdFormatError error" when {
      "an unsupported tax year is supplied" in new Test {
        validate(employmentId = "BAD_EMP_ID") shouldBe singleError(EmploymentIdFormatError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validate("BAD_NINO", "BAD_TAX_YEAR") shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
