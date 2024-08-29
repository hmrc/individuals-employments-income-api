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

import play.api.libs.json._
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import support.UnitSpec
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}

import java.time.{Clock, Instant, ZoneOffset}

class AddCustomEmploymentValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2020-21"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validRequestBodyJson: JsValue = Json.parse(
    """{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
      |""".stripMargin
  )

  trait Test {
    implicit val clock: Clock = Clock.fixed(Instant.parse("2022-06-01T00:00:00Z"), ZoneOffset.UTC)

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 body: JsValue = validRequestBodyJson,
                 temporalValidationEnabled: Boolean = true): Either[ErrorWrapper, AddCustomEmploymentRequest] =
      new AddCustomEmploymentValidator(nino, taxYear, body, temporalValidationEnabled, mockAppConfig)
        .validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2020-21")
  }

  "validate" should {
    "return a request object" when {
      "valid" in new Test {
        validate() shouldBe Right(AddCustomEmploymentRequest(parsedNino, parsedTaxYear, validRequestBodyJson.as[AddCustomEmploymentRequestBody]))
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

    "return RuleTaxYearNotEndedError error" when {
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
        val body: JsValue = validRequestBodyJson
          .update("startDate", Json.toJson(ty.startDate))
          .update("cessationDate", Json.toJson(ty.endDate))

        validate(
          taxYear = ty.asMtd,
          body = body,
          temporalValidationEnabled = false
        ) shouldBe
          Right(AddCustomEmploymentRequest(parsedNino, ty, body.as[AddCustomEmploymentRequestBody]))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validate(body = JsObject.empty) shouldBe singleError(RuleIncorrectOrEmptyBodyError)
      }

      "mandatory fields are missing" in new Test {
        val paths = List(
          "/employerName",
          "/occupationalPension",
          "/startDate"
        )

        validate(body = Json.parse("""{"field": "value"}""")) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPaths(paths))
      }
    }

    "return EmployerNameFormatError error" when {
      "the format of the employer name is not valid" in new Test {
        validate(body = validRequestBodyJson.update("employerName", JsString("x" * 75))) shouldBe
          singleError(EmployerNameFormatError)
      }
    }

    "return EmployerRefFormatError error" when {
      "the format of the employer ref is not valid" in new Test {
        validate(body = validRequestBodyJson.update("employerRef", JsString("BAD_REF"))) shouldBe
          singleError(EmployerRefFormatError)
      }
    }

    "return StartDateFormatError error" when {
      "the format of the start date is invalid" in new Test {
        validate(body = validRequestBodyJson.update("startDate", JsString("BAD_DATE"))) shouldBe
          singleError(StartDateFormatError)
      }
    }

    "return CessationDateFormatError error" when {
      "the format of the cessation date is invalid" in new Test {
        validate(body = validRequestBodyJson.update("cessationDate", JsString("BAD_DATE"))) shouldBe
          singleError(CessationDateFormatError)
      }
    }

    "return RuleCessationDateBeforeStartDateError error" when {
      "the cessation date is before the start date" in new Test {
        validate(body = validRequestBodyJson
          .update("startDate", JsString("2020-07-01"))
          .update("cessationDate", JsString("2020-06-01"))) shouldBe
          singleError(RuleCessationDateBeforeStartDateError)
      }
    }

    "return RuleStartDateAfterTaxYearEndError error" when {
      "the start date is after the tax year end" in new Test {
        validate(body = validRequestBodyJson
          .update("startDate", JsString("2021-06-01"))
          .update("cessationDate", JsString("2021-07-01"))) shouldBe
          singleError(RuleStartDateAfterTaxYearEndError)
      }
    }

    "return RuleCessationDateBeforeTaxYearStartError error" when {
      "the start date is after the tax year end" in new Test {
        validate(body = validRequestBodyJson
          .update("startDate", JsString("2020-01-01"))
          .update("cessationDate", JsString("2020-01-02"))) shouldBe
          singleError(RuleCessationDateBeforeTaxYearStartError)
      }
    }

    "return PayrollIdFormatError error" when {
      "the format of the dateOfEvent is not valid" in new Test {
        validate(body = validRequestBodyJson.update("payrollId", JsString("x" * 39))) shouldBe
          singleError(PayrollIdFormatError)
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
