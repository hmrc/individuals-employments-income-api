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

import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v1.mocks.validators.MockRetrieveNonPayeEmploymentValidator
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

class RetrieveNonPayeEmploymentIncomeValidatorSpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2020-21"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  trait Test extends MockRetrieveNonPayeEmploymentValidator {

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 maybeSource: Option[String] = None): Either[ErrorWrapper, RetrieveNonPayeEmploymentIncomeRequest] =
      new RetrieveNonPayeEmploymentIncomeValidator(nino, taxYear, maybeSource, mockAppConfig).validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2020-21")
  }

  "parse" should {
    "return a request object" when {
      def validateSuccessfullyWith(sourceString: String, expectedRequest: RetrieveNonPayeEmploymentIncomeRequest): Unit =
        s"valid request data is supplied with source $sourceString" in new Test {
          validate(maybeSource = Some(sourceString)) shouldBe Right(expectedRequest)
        }

      val input = Seq(
        ("hmrc-held", RetrieveNonPayeEmploymentIncomeRequest(parsedNino, parsedTaxYear, MtdSourceEnum.`hmrc-held`)),
        ("latest", RetrieveNonPayeEmploymentIncomeRequest(parsedNino, parsedTaxYear, MtdSourceEnum.latest)),
        ("user", RetrieveNonPayeEmploymentIncomeRequest(parsedNino, parsedTaxYear, MtdSourceEnum.user))
      )

      input.foreach(args => (validateSuccessfullyWith _).tupled(args))

      "default to 'latest' source" in new Test {
        validate(maybeSource = None) shouldBe Right(RetrieveNonPayeEmploymentIncomeRequest(parsedNino, parsedTaxYear, MtdSourceEnum.latest))
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
        validate(taxYear = "2019-21") shouldBe singleError(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in new Test {
        validate(taxYear = "2018-19") shouldBe singleError(RuleTaxYearNotSupportedError)
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
