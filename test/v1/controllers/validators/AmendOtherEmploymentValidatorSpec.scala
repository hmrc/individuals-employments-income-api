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
import play.api.libs.json._
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import support.UnitSpec
import v1.models.request.amendOtherEmployment.{AmendOtherEmploymentRequest, AmendOtherEmploymentRequestBody}

class AmendOtherEmploymentValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "correlationId"
  private val validNino                      = "AA123456B"
  private val validTaxYear                   = "2023-24"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "emi",
      |        "dateOfOptionGrant": "2019-11-20",
      |        "dateOfEvent": "2019-11-20",
      |        "optionNotExercisedButConsiderationReceived": true,
      |        "amountOfConsiderationReceived": 23122.22,
      |        "noOfSharesAcquired": 1,
      |        "classOfSharesAcquired": "FIRST",
      |        "exercisePrice": 12.22,
      |        "amountPaidForOption": 123.22,
      |        "marketValueOfSharesOnExcise": 1232.22,
      |        "profitOnOptionExercised": 1232.33,
      |        "employersNicPaid": 2312.22,
      |        "taxableAmount" : 2132.22
      |      }
      |  ],
      |  "sharesAwardedOrReceived": [
      |       {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "sip",
      |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |        "noOfShareSecuritiesAwarded": 11,
      |        "classOfShareAwarded": "FIRST",
      |        "dateSharesAwarded" : "2019-11-20",
      |        "sharesSubjectToRestrictions": true,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": 2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |        "amountPaidForSharesOnAward": 123.22,
      |        "marketValueAfterRestrictionsLifted": 1232.22,
      |        "taxableAmount": 12321.22
      |       }
      |  ],
      |  "disability":
      |    {
      |      "customerReference": "OTHEREmp123A",
      |      "amountDeducted": 5000.99
      |    },
      |  "foreignService":
      |    {
      |      "customerReference": "OTHEREmp999A",
      |      "amountDeducted": 7000.99
      |    },
      |   "lumpSums": [
      |     {
      |      "employerName": "BPDTS Ltd",
      |      "employerRef": "123/AB456",
      |      "taxableLumpSumsAndCertainIncome":
      |         {
      |           "amount": 5000.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "benefitFromEmployerFinancedRetirementScheme":
      |         {
      |           "amount": 5000.99,
      |           "exemptAmount": 2345.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsOverExemption":
      |         {
      |           "amount": 5000.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsUnderExemption":
      |         {
      |           "amount": 5000.99
      |         }
      |     }
      |   ]
      |}
    """.stripMargin
  )

  trait Test {

    def validate(nino: String = validNino,
                 taxYear: String = validTaxYear,
                 body: JsValue = validRequestBodyJson): Either[ErrorWrapper, AmendOtherEmploymentRequest] =
      new AmendOtherEmploymentValidator(nino, taxYear, body, mockAppConfig)
        .validateAndWrapResult()

    def singleError(error: MtdError): Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

    MockedAppConfig.minimumPermittedTaxYear returns TaxYear.fromMtd("2020-21")
  }

  private def expectValueFormatError(body: JsNumber => JsValue, expectedPath: String): Unit = s"for $expectedPath" when {
    def doTest(value: JsNumber): Unit = new Test {
      validate(body = body(value)) shouldBe singleError(ValueFormatError.forPathAndRange(expectedPath, "0", "99999999999.99"))
    }

    "value is out of range" in doTest(JsNumber(99999999999.99 + 0.01))
    "value is negative" in doTest(JsNumber(-0.01))
  }

  private def expectValueFormatErrorNonNegInt(body: JsNumber => JsValue, expectedPath: String): Unit = s"for $expectedPath" when {
    def doTest(value: JsNumber): Unit = new Test {
      validate(body = body(value)) shouldBe singleError(ValueFormatError.forPathAndMin(expectedPath, "0"))
    }

    "value is negative" in doTest(JsNumber(-1))
  }

  "validate" should {
    "return a request object" when {
      "valid" in new Test {
        validate() shouldBe Right(AmendOtherEmploymentRequest(parsedNino, parsedTaxYear, validRequestBodyJson.as[AmendOtherEmploymentRequestBody]))
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

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validate(body = JsObject.empty) shouldBe singleError(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validate(body = Json.parse("""{"field": "value"}""")) shouldBe singleError(RuleIncorrectOrEmptyBodyError)
      }

      "mandatory array fields are missing" in new Test {
        val paths = List(
          "/shareOption/0/amountOfConsiderationReceived",
          "/shareOption/0/amountPaidForOption",
          "/shareOption/0/classOfSharesAcquired",
          "/shareOption/0/dateOfEvent",
          "/shareOption/0/dateOfOptionGrant",
          "/shareOption/0/employerName",
          "/shareOption/0/employersNicPaid",
          "/shareOption/0/exercisePrice",
          "/shareOption/0/marketValueOfSharesOnExcise",
          "/shareOption/0/noOfSharesAcquired",
          "/shareOption/0/optionNotExercisedButConsiderationReceived",
          "/shareOption/0/profitOnOptionExercised",
          "/shareOption/0/schemePlanType",
          "/shareOption/0/taxableAmount"
        )

        validate(body = validRequestBodyJson
          .removeProperty("shareOption")
          .update("shareOption", JsArray(Seq(JsObject.empty)))) shouldBe
          singleError(RuleIncorrectOrEmptyBodyError.withPaths(paths))
      }
    }

    "validating shareOption fields" should {
      val itemJson = Json.parse("""{
          |  "employerName": "Company Ltd",
          |  "employerRef" : "123/AB456",
          |  "schemePlanType": "emi",
          |  "dateOfOptionGrant": "2019-11-20",
          |  "dateOfEvent": "2019-11-20",
          |  "optionNotExercisedButConsiderationReceived": true,
          |  "amountOfConsiderationReceived": 23122.22,
          |  "noOfSharesAcquired": 1,
          |  "classOfSharesAcquired": "FIRST",
          |  "exercisePrice": 12.22,
          |  "amountPaidForOption": 123.22,
          |  "marketValueOfSharesOnExcise": 1232.22,
          |  "profitOnOptionExercised": 1232.33,
          |  "employersNicPaid": 2312.22,
          |  "taxableAmount" : 2132.22
          |}
          """.stripMargin)

      def body(value: JsValue) = Json.parse(s"""
                                               |{
                                               |  "shareOption": [$value]
                                               |}""".stripMargin)

      def fromField(field: String)(value: JsValue) = body(itemJson.update(field, value))

      "return EmployerNameFormatError error" when {
        "the format of the employer name is not valid" in new Test {
          validate(body = body(itemJson.update("employerName", JsString("x" * 106)))) shouldBe
            singleError(EmployerNameFormatError.withPath("/shareOption/0/employerName"))
        }
      }

      "return EmployerRefFormatError error" when {
        "the format of the employer ref is not valid" in new Test {
          validate(body = body(itemJson.update("employerRef", JsString("BAD_REF")))) shouldBe
            singleError(EmployerRefFormatError.withPath("/shareOption/0/employerRef"))
        }
      }

      "return SchemePlanTypeFormatError error" when {
        "the format of the scheme plan is not valid" in new Test {
          validate(body = body(itemJson.update("schemePlanType", JsString("BAD_SCHEME")))) shouldBe
            singleError(SchemePlanTypeFormatError.withPath("/shareOption/0/schemePlanType"))
        }
      }

      "return DateFormatError error" when {
        "the format of the dateOfOptionGrant is not valid" in new Test {
          validate(body = body(itemJson.update("dateOfOptionGrant", JsString("BAD_DATE")))) shouldBe
            singleError(DateFormatError.withPath("/shareOption/0/dateOfOptionGrant"))
        }
      }

      "return DateFormatError error" when {
        "the format of the dateOfEvent is not valid" in new Test {
          validate(body = body(itemJson.update("dateOfEvent", JsString("BAD_DATE")))) shouldBe
            singleError(DateFormatError.withPath("/shareOption/0/dateOfEvent"))
        }
      }

      "return ClassOfSharesAcquiredFormatError error" when {
        "the format of the classOfSharesAcquired is not valid" in new Test {
          validate(body = body(itemJson.update("classOfSharesAcquired", JsString("x" * 91)))) shouldBe
            singleError(ClassOfSharesAcquiredFormatError.withPath("/shareOption/0/classOfSharesAcquired"))
        }
      }

      expectValueFormatErrorNonNegInt(fromField("noOfSharesAcquired"), "/shareOption/0/noOfSharesAcquired")
      expectValueFormatError(fromField("amountOfConsiderationReceived"), "/shareOption/0/amountOfConsiderationReceived")
      expectValueFormatError(fromField("exercisePrice"), "/shareOption/0/exercisePrice")
      expectValueFormatError(fromField("amountPaidForOption"), "/shareOption/0/amountPaidForOption")
      expectValueFormatError(fromField("marketValueOfSharesOnExcise"), "/shareOption/0/marketValueOfSharesOnExcise")
      expectValueFormatError(fromField("profitOnOptionExercised"), "/shareOption/0/profitOnOptionExercised")
      expectValueFormatError(fromField("employersNicPaid"), "/shareOption/0/employersNicPaid")
      expectValueFormatError(fromField("taxableAmount"), "/shareOption/0/taxableAmount")
    }

    "validating sharesAwardedOrReceived fields" should {
      val itemJson =
        Json.parse("""{
                       |  "employerName": "Company Ltd",
                       |  "employerRef" : "123/AB456",
                       |  "schemePlanType": "sip",
                       |  "dateSharesCeasedToBeSubjectToPlan": "2020-01-02",
                       |  "noOfShareSecuritiesAwarded": 11,
                       |  "classOfShareAwarded": "FIRST",
                       |  "dateSharesAwarded" : "2020-01-01",
                       |  "sharesSubjectToRestrictions": true,
                       |  "electionEnteredIgnoreRestrictions": false,
                       |  "actualMarketValueOfSharesOnAward": 2123.22,
                       |  "unrestrictedMarketValueOfSharesOnAward": 123.22,
                       |  "amountPaidForSharesOnAward": 123.22,
                       |  "marketValueAfterRestrictionsLifted": 1232.22,
                       |  "taxableAmount": 12321.22
                       |}""".stripMargin)

      def body(value: JsValue) = Json.parse(s"""
                                                 |{
                                                 |  "sharesAwardedOrReceived": [$value]
                                                 |}""".stripMargin)

      def fromField(field: String)(value: JsValue) = body(itemJson.update(field, value))

      "return EmployerNameFormatError error" when {
        "the format of the employer name is not valid" in new Test {
          validate(body = body(itemJson.update("employerName", JsString("x" * 106)))) shouldBe
            singleError(EmployerNameFormatError.withPath("/sharesAwardedOrReceived/0/employerName"))
        }
      }

      "return EmployerRefFormatError error" when {
        "the format of the employer ref is not valid" in new Test {
          validate(body = body(itemJson.update("employerRef", JsString("BAD_REF")))) shouldBe
            singleError(EmployerRefFormatError.withPath("/sharesAwardedOrReceived/0/employerRef"))
        }
      }

      "return SchemePlanTypeFormatError error" when {
        "the format of the scheme plan is not valid" in new Test {
          validate(body = body(itemJson.update("schemePlanType", JsString("BAD")))) shouldBe
            singleError(SchemePlanTypeFormatError.withPath("/sharesAwardedOrReceived/0/schemePlanType"))
        }
      }

      "return DateFormatError error" when {
        "the format of the dateSharesCeasedToBeSubjectToPlan is not valid" in new Test {
          validate(body = body(itemJson.update("dateSharesCeasedToBeSubjectToPlan", JsString("BAD_DATE")))) shouldBe
            singleError(DateFormatError.withPath("/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan"))
        }
      }

      "return DateFormatError error" when {
        "the format of the dateSharesAwarded is not valid" in new Test {
          validate(body = body(itemJson.update("dateSharesAwarded", JsString("BAD_DATE")))) shouldBe
            singleError(DateFormatError.withPath("/sharesAwardedOrReceived/0/dateSharesAwarded"))
        }
      }

      "return ClassOfSharesAwardedFormatError error" when {
        "the format of the classOfShareAwarded is not valid" in new Test {
          validate(body = body(itemJson.update("classOfShareAwarded", JsString("x" * 91)))) shouldBe
            singleError(ClassOfSharesAwardedFormatError.withPath("/sharesAwardedOrReceived/0/classOfShareAwarded"))
        }
      }

      expectValueFormatErrorNonNegInt(fromField("noOfShareSecuritiesAwarded"), "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded")
      expectValueFormatError(fromField("actualMarketValueOfSharesOnAward"), "/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward")
      expectValueFormatError(fromField("unrestrictedMarketValueOfSharesOnAward"), "/sharesAwardedOrReceived/0/unrestrictedMarketValueOfSharesOnAward")
      expectValueFormatError(fromField("amountPaidForSharesOnAward"), "/sharesAwardedOrReceived/0/amountPaidForSharesOnAward")
      expectValueFormatError(fromField("marketValueAfterRestrictionsLifted"), "/sharesAwardedOrReceived/0/marketValueAfterRestrictionsLifted")
      expectValueFormatError(fromField("taxableAmount"), "/sharesAwardedOrReceived/0/taxableAmount")
    }

    "validating disability fields" should {
      "return CustomerRefFormatError error" when {
        "the format of the customer ref is not valid" in new Test {
          validate(body = validRequestBodyJson.update("/disability/customerReference", JsString("x" * 91))) shouldBe
            singleError(CustomerRefFormatError.withPath("/disability/customerReference"))
        }
      }

      expectValueFormatError(number => validRequestBodyJson.update("/disability/amountDeducted", number), "/disability/amountDeducted")
    }

    "validating foreignService fields" should {

      "return CustomerRefFormatError error" when {
        "the format of the customer ref is not valid" in new Test {
          validate(body = validRequestBodyJson.update("/foreignService/customerReference", JsString("x" * 91))) shouldBe
            singleError(CustomerRefFormatError.withPath("/foreignService/customerReference"))
        }
      }

      expectValueFormatError(number => validRequestBodyJson.update("/foreignService/amountDeducted", number), "/foreignService/amountDeducted")
    }

    "validating lumpSums fields" should {
      val itemJson =
        Json.parse("""{
                     |  "employerName": "BPDTS Ltd",
                     |  "employerRef": "123/AB456",
                     |  "taxableLumpSumsAndCertainIncome":
                     |    {
                     |      "amount": 5000.99,
                     |      "taxPaid": 3333.33,
                     |      "taxTakenOffInEmployment": true
                     |    },
                     |  "benefitFromEmployerFinancedRetirementScheme":
                     |    {
                     |      "amount": 5000.99,
                     |      "exemptAmount": 2345.99,
                     |      "taxPaid": 3333.33,
                     |      "taxTakenOffInEmployment": true
                     |    },
                     |  "redundancyCompensationPaymentsOverExemption":
                     |    {
                     |      "amount": 5000.99,
                     |      "taxPaid": 3333.33,
                     |      "taxTakenOffInEmployment": true
                     |    },
                     |  "redundancyCompensationPaymentsUnderExemption":
                     |    {
                     |      "amount": 5000.99
                     |    }
                     |}""".stripMargin)

      def body(value: JsValue) = Json.parse(s"""
                                               |{
                                               |  "lumpSums": [$value]
                                               |}""".stripMargin)

      def fromField(field: String)(value: JsValue) = body(itemJson.update(field, value))

      "return EmployerNameFormatError error" when {
        "the format of the employer name is not valid" in new Test {
          validate(body = body(itemJson.update("employerName", JsString("x" * 106)))) shouldBe
            singleError(EmployerNameFormatError.withPath("/lumpSums/0/employerName"))
        }
      }

      "return EmployerRefFormatError error" when {
        "the format of the employer ref is not valid" in new Test {
          validate(body = body(itemJson.update("employerRef", JsString("BAD_REF")))) shouldBe
            singleError(EmployerRefFormatError.withPath("/lumpSums/0/employerRef"))
        }
      }

      "Return RuleLumpSumsError" when {
        "all lump sums sections are missing" in new Test {
          val body = Json.parse(s"""
                        |{
                        |  "lumpSums": [
                        |    {
                        |      "employerName": "BPDTS Ltd",
                        |      "employerRef": "123/AB456"
                        |    }
                        |  ]
                        |}""".stripMargin)

          validate(body = body) shouldBe
            singleError(RuleLumpSumsError.withPath("/lumpSums/0"))
        }
      }

      "not return RuleLumpSumsError" when {
        val sections = Set(
          "taxableLumpSumsAndCertainIncome",
          "benefitFromEmployerFinancedRetirementScheme",
          "redundancyCompensationPaymentsOverExemption",
          "redundancyCompensationPaymentsUnderExemption"
        )

        def noErrorWhenOnlyKeep(keptSection: String): Unit =
          s"only contain $keptSection" in new Test {
            val lumpSumWithKeptSection = (sections - keptSection).foldLeft(itemJson)(_ removeProperty _)

            validate(body = body(lumpSumWithKeptSection)) shouldBe a[Right[_, _]]
          }

        sections.foreach(behave like noErrorWhenOnlyKeep(_))
      }

      expectValueFormatError(fromField("taxableLumpSumsAndCertainIncome/amount"), "/lumpSums/0/taxableLumpSumsAndCertainIncome/amount")
      expectValueFormatError(fromField("taxableLumpSumsAndCertainIncome/taxPaid"), "/lumpSums/0/taxableLumpSumsAndCertainIncome/taxPaid")

      expectValueFormatError(
        fromField("benefitFromEmployerFinancedRetirementScheme/amount"),
        "/lumpSums/0/benefitFromEmployerFinancedRetirementScheme/amount")
      expectValueFormatError(
        fromField("benefitFromEmployerFinancedRetirementScheme/exemptAmount"),
        "/lumpSums/0/benefitFromEmployerFinancedRetirementScheme/exemptAmount")
      expectValueFormatError(
        fromField("benefitFromEmployerFinancedRetirementScheme/taxPaid"),
        "/lumpSums/0/benefitFromEmployerFinancedRetirementScheme/taxPaid")

      expectValueFormatError(
        fromField("redundancyCompensationPaymentsOverExemption/amount"),
        "/lumpSums/0/redundancyCompensationPaymentsOverExemption/amount")
      expectValueFormatError(
        fromField("redundancyCompensationPaymentsOverExemption/taxPaid"),
        "/lumpSums/0/redundancyCompensationPaymentsOverExemption/taxPaid")

      expectValueFormatError(
        fromField("redundancyCompensationPaymentsUnderExemption/amount"),
        "/lumpSums/0/redundancyCompensationPaymentsUnderExemption/amount")
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validate("BAD_NINO", "BAD_TAX_YEAR") shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
