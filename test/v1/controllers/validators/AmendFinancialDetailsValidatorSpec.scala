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

import api.models.domain.{EmploymentId, Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsArray, JsBoolean, JsNumber, JsObject, JsValue, Json}
import support.UnitSpec
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import java.time.{Clock, Instant, ZoneOffset}

class AmendFinancialDetailsValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2023-24"
  private val validEmploymentId              = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val parsedNino         = Nino(validNino)
  private val parsedTaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedEmploymentId = EmploymentId(validEmploymentId)

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": 13343.45,
      |                "pglDeductionAmount": 24242.56
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": 455.67,
      |            "assets": 435.54,
      |            "assetTransfer": 24.58,
      |            "beneficialLoan": 33.89,
      |            "car": 3434.78,
      |            "carFuel": 34.56,
      |            "educationalServices": 445.67,
      |            "entertaining": 434.45,
      |            "expenses": 3444.32,
      |            "medicalInsurance": 4542.47,
      |            "telephone": 243.43,
      |            "service": 45.67,
      |            "taxableExpenses": 24.56,
      |            "van": 56.29,
      |            "vanFuel": 14.56,
      |            "mileage": 34.23,
      |            "nonQualifyingRelocationExpenses": 54.62,
      |            "nurseryPlaces": 84.29,
      |            "otherItems": 67.67,
      |            "paymentsOnEmployeesBehalf": 67.23,
      |            "personalIncidentalExpenses": 74.29,
      |            "qualifyingRelocationExpenses": 78.24,
      |            "employerProvidedProfessionalSubscriptions": 84.56,
      |            "employerProvidedServices": 56.34,
      |            "incomeTaxPaidByDirector": 67.34,
      |            "travelAndSubsistence": 56.89,
      |            "vouchersAndCreditCards": 34.90,
      |            "nonCash": 23.89
      |        },
      |        "offPayrollWorker" : true
      |    }
      |}
    """.stripMargin
  )

  trait Test {
    implicit val clock: Clock = Clock.fixed(Instant.parse("2022-06-01T00:00:00Z"), ZoneOffset.UTC)

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 employmentId: String = validEmploymentId,
                 body: JsValue = validRequestBodyJson): Either[ErrorWrapper, AmendFinancialDetailsRequest] =
      new AmendFinancialDetailsValidator(nino, taxYear, employmentId, body, mockAppConfig)
        .validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2020-21")
  }

  private def update(field: String)(value: JsValue) = validRequestBodyJson.update(field, value)

  def expectValueFormatError(path: String, min: BigDecimal = 0, max: BigDecimal = 99999999999.99): Unit =
    expectValueFormatErrorUsing(update(path), path, min, max)

  def expectValueFormatErrorUsing(body: JsNumber => JsValue, expectedPath: String, min: BigDecimal = 0, max: BigDecimal = 99999999999.99): Unit =
    s"for $expectedPath" when {
      def expectError(value: JsNumber): Unit = new Test {
        validate(body = body(value)) shouldBe singleError(ValueFormatError.forPathAndRange(expectedPath, min.toString, max.toString))
      }

      def expectNoError(value: JsNumber): Unit = new Test {
        validate(body = body(value)) shouldBe a[Right[_, _]]
      }

      "expect ValueFormatError when value is larger the max" in expectError(JsNumber(max + 0.01))
      "expect ValueFormatError when value is smaller than min" in expectError(JsNumber(min - 0.01))
      "expect no error when value is in min" in expectNoError(JsNumber(max))
      "expect no error when value is in max" in expectNoError(JsNumber(min))
    }

  "validate" should {
    "return a request object" when {
      "valid" in new Test {
        validate() shouldBe Right(
          AmendFinancialDetailsRequest(parsedNino, parsedTaxYear, parsedEmploymentId, validRequestBodyJson.as[AmendFinancialDetailsRequestBody]))
      }
    }

    "return a request object" when {
      "valid without offPayrollWorker included for a TYS tax year" in new Test {
        private val taxYearString           = "2022-23"
        private val bodyWithoutOpw: JsValue = validRequestBodyJson.removeProperty("/employment/offPayrollWorker")

        validate(
          taxYear = taxYearString,
          body = bodyWithoutOpw
        ) shouldBe Right(
          AmendFinancialDetailsRequest(
            parsedNino,
            TaxYear.fromMtd(taxYearString),
            parsedEmploymentId,
            bodyWithoutOpw.as[AmendFinancialDetailsRequestBody]))
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

    "return EmploymentIdFormatError error" when {
      "the format of the employmentId is not valid" in new Test {
        validate(employmentId = "BAD_EMP_ID") shouldBe singleError(EmploymentIdFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validate(body = JsObject.empty) shouldBe singleError(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validate(body = Json.parse("""{"field": "value"}""")) shouldBe singleError(RuleIncorrectOrEmptyBodyError.withPath("/employment"))
      }

      "mandatory fields are not provided" in new Test {
        validate(body = validRequestBodyJson
          .removeProperty("/employment/pay/taxablePayToDate")
          .removeProperty("/employment/pay/totalTaxToDate")) shouldBe
          singleError(
            RuleIncorrectOrEmptyBodyError.withPaths(
              Seq(
                "/employment/pay/taxablePayToDate",
                "/employment/pay/totalTaxToDate"
              )))
      }

      "studentLoans object is provided with no fields" in new Test {
        validate(body = validRequestBodyJson
          .replaceWithEmptyObject("/employment/deductions/studentLoans")) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPath("/employment/deductions/studentLoans"))
      }

      "deductions object is provided with no fields" in new Test {
        validate(body = validRequestBodyJson
          .replaceWithEmptyObject("/employment/deductions")) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPath("/employment/deductions"))
      }

      "benefitsInKind object is provided with no fields" in new Test {
        validate(body = validRequestBodyJson
          .replaceWithEmptyObject("/employment/benefitsInKind")) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPath("/employment/benefitsInKind"))
      }

      "fields are of an incorrect type" in new Test {
        val paths: Seq[String] = Seq(
          "/employment/benefitsInKind/accommodation",
          "/employment/deductions/studentLoans/uglDeductionAmount",
          "/employment/pay/taxablePayToDate"
        )

        validate(body = validRequestBodyJson
          .update("/employment/benefitsInKind/accommodation", JsBoolean(false))
          .update("/employment/deductions/studentLoans/uglDeductionAmount", JsArray())
          .update("/employment/pay/taxablePayToDate", JsArray())) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPaths(paths))
      }
    }

    "return RuleMissingOffPayrollWorker error when offPayrollWorker is missing for a taxYear 23-24 or later" in new Test {
      validate(
        taxYear = "2023-24",
        body = validRequestBodyJson
          .removeProperty("/employment/offPayrollWorker")) shouldBe
        singleError(RuleMissingOffPayrollWorker)
    }

    "return RuleNotAllowedOffPayrollWorker error when offPayrollWorker is provided for a taxYear before 23-24" in new Test {
      validate(
        taxYear = "2022-23",
        body = validRequestBodyJson
          .update("/employment/offPayrollWorker", JsBoolean(true))) shouldBe
        singleError(RuleNotAllowedOffPayrollWorker)
    }

    "validating pay fields" should {
      expectValueFormatError("/employment/pay/taxablePayToDate")
      expectValueFormatError("/employment/pay/totalTaxToDate", min = -99999999999.99)
    }

    "validating deductions fields" should {
      expectValueFormatError("/employment/deductions/studentLoans/uglDeductionAmount")
      expectValueFormatError("/employment/deductions/studentLoans/pglDeductionAmount")
    }

    "validating benefitsInKind fields" should {
      expectValueFormatError("/employment/benefitsInKind/accommodation")
      expectValueFormatError("/employment/benefitsInKind/assets")
      expectValueFormatError("/employment/benefitsInKind/assetTransfer")
      expectValueFormatError("/employment/benefitsInKind/beneficialLoan")
      expectValueFormatError("/employment/benefitsInKind/car")
      expectValueFormatError("/employment/benefitsInKind/carFuel")
      expectValueFormatError("/employment/benefitsInKind/educationalServices")
      expectValueFormatError("/employment/benefitsInKind/entertaining")
      expectValueFormatError("/employment/benefitsInKind/expenses")
      expectValueFormatError("/employment/benefitsInKind/medicalInsurance")
      expectValueFormatError("/employment/benefitsInKind/telephone")
      expectValueFormatError("/employment/benefitsInKind/service")
      expectValueFormatError("/employment/benefitsInKind/taxableExpenses")
      expectValueFormatError("/employment/benefitsInKind/van")
      expectValueFormatError("/employment/benefitsInKind/vanFuel")
      expectValueFormatError("/employment/benefitsInKind/mileage")
      expectValueFormatError("/employment/benefitsInKind/nonQualifyingRelocationExpenses")
      expectValueFormatError("/employment/benefitsInKind/nurseryPlaces")
      expectValueFormatError("/employment/benefitsInKind/otherItems")
      expectValueFormatError("/employment/benefitsInKind/paymentsOnEmployeesBehalf")
      expectValueFormatError("/employment/benefitsInKind/personalIncidentalExpenses")
      expectValueFormatError("/employment/benefitsInKind/qualifyingRelocationExpenses")
      expectValueFormatError("/employment/benefitsInKind/employerProvidedProfessionalSubscriptions")
      expectValueFormatError("/employment/benefitsInKind/employerProvidedServices")
      expectValueFormatError("/employment/benefitsInKind/incomeTaxPaidByDirector")
      expectValueFormatError("/employment/benefitsInKind/travelAndSubsistence")
      expectValueFormatError("/employment/benefitsInKind/vouchersAndCreditCards")
      expectValueFormatError("/employment/benefitsInKind/nonCash")
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validate("BAD_NINO", "BAD_TAX_YEAR") shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
