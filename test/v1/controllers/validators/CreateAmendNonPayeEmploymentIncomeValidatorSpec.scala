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

import shared.config.MockAppConfig
import play.api.libs.json.{JsObject, JsValue, Json}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import support.UnitSpec
import v1.models.request.createAmendNonPayeEmployment.{CreateAmendNonPayeEmploymentRequest, CreateAmendNonPayeEmploymentRequestBody}

import java.time.{Clock, Instant, ZoneOffset}

class CreateAmendNonPayeEmploymentIncomeValidatorSpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2020-21"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  val validTips = 100.23

  private val validRequestBodyJson: JsValue = Json.parse(s"""
                                                            |{
                                                            |  "tips": $validTips
                                                            |}
                                                            |""".stripMargin)

  trait Test {
    implicit val clock: Clock = Clock.fixed(Instant.parse("2022-06-01T00:00:00Z"), ZoneOffset.UTC)

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 body: JsValue = validRequestBodyJson,
                 temporalValidationEnabled: Boolean = true): Either[ErrorWrapper, CreateAmendNonPayeEmploymentRequest] =
      new CreateAmendNonPayeEmploymentIncomeValidator(nino, taxYear, body, temporalValidationEnabled, mockAppConfig)
        .validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2020-21")
  }

  "validate" should {
    "return a request object" when {
      "valid" in new Test {
        validate() shouldBe Right(CreateAmendNonPayeEmploymentRequest(parsedNino, parsedTaxYear, CreateAmendNonPayeEmploymentRequestBody(validTips)))
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

    "return RuleTaxYearNotEnded error" when {
      "a tax year in the current year is supplied" in new Test {
        val ty = TaxYear.currentTaxYear
        validate(taxYear = ty.asMtd, temporalValidationEnabled = true) shouldBe singleError(RuleTaxYearNotEndedError)
      }
    }

    "not return RuleTaxYearNotEnded error" when {
      "a tax year in the current year is supplied if temporal validation is not enabled" in new Test {
        val ty = TaxYear.currentTaxYear
        validate(taxYear = ty.asMtd, temporalValidationEnabled = false) shouldBe
          Right(CreateAmendNonPayeEmploymentRequest(parsedNino, ty, body = CreateAmendNonPayeEmploymentRequestBody(validTips)))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validate(body = JsObject.empty) shouldBe singleError(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validate(body = Json.parse("""{"field": "value"}""")) shouldBe singleError(RuleIncorrectOrEmptyBodyError.withPath("/tips"))
      }

      "the submitted request body where fields have incorrect type" in new Test {
        validate(body = Json.parse("""{ "tips": true }""")) shouldBe singleError(RuleIncorrectOrEmptyBodyError.withPath("/tips"))
      }
    }

    "return TipsFormatError" when {
      "passed out of range tips" in new Test {
        validate(body = Json.parse("""{ "tips": -100.99 }""")) shouldBe singleError(ValueFormatError.withPath("/tips"))
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
