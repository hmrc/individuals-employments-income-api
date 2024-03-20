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

import api.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.request.amendOtherEmployment.AmendOtherEmploymentRawData

class AmendOtherEmploymentValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

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
      |      },
      |      {
      |        "employerName": "SecondCom Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "csop",
      |        "dateOfOptionGrant": "2020-09-12",
      |        "dateOfEvent": "2020-09-12",
      |        "optionNotExercisedButConsiderationReceived": false,
      |        "amountOfConsiderationReceived": 5000.99,
      |        "noOfSharesAcquired": 200,
      |        "classOfSharesAcquired": "Ordinary shares",
      |        "exercisePrice": 1000.99,
      |        "amountPaidForOption": 5000.99,
      |        "marketValueOfSharesOnExcise": 5500.99,
      |        "profitOnOptionExercised": 3333.33,
      |        "employersNicPaid": 2000.22,
      |        "taxableAmount" : 2555.55
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
      |       },
      |       {
      |        "employerName": "SecondCom Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "other",
      |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
      |        "noOfShareSecuritiesAwarded": 299,
      |        "classOfShareAwarded": "Ordinary shares",
      |        "dateSharesAwarded" : "2020-09-12",
      |        "sharesSubjectToRestrictions": false,
      |        "electionEnteredIgnoreRestrictions": true,
      |        "actualMarketValueOfSharesOnAward": 5000.99,
      |        "unrestrictedMarketValueOfSharesOnAward": 5432.21,
      |        "amountPaidForSharesOnAward": 6000.99,
      |        "marketValueAfterRestrictionsLifted": 3333.33,
      |        "taxableAmount": 98765.99
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
      |     },
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
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {}
      |   ]
      |}
    """.stripMargin
  )

  private val invalidEmployerNameJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
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
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidEmployerRefJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "InvalidReference",
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
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidSchemePlanTypeJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "NotAScheme",
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
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidDateJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "emi",
      |        "dateOfOptionGrant": "19-11-20",
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
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidClassOfSharesAcquiredJson: JsValue = Json.parse(
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
      |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
      |        "exercisePrice": 12.22,
      |        "amountPaidForOption": 123.22,
      |        "marketValueOfSharesOnExcise": 1232.22,
      |        "profitOnOptionExercised": 1232.33,
      |        "employersNicPaid": 2312.22,
      |        "taxableAmount" : 2132.22
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidClassOfSharesAwardedJson: JsValue = Json.parse(
    """
      |{
      |  "sharesAwardedOrReceived": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "sip",
      |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |        "noOfShareSecuritiesAwarded": 11,
      |        "classOfShareAwarded": "This ClassOfShares Awarded string is 91 characters long ---------------------------------91",
      |        "dateSharesAwarded" : "2019-11-20",
      |        "sharesSubjectToRestrictions": true,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": 2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |        "amountPaidForSharesOnAward": 123.22,
      |        "marketValueAfterRestrictionsLifted": 1232.22,
      |        "taxableAmount": 12321.22
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidCustomerRefJson: JsValue = Json.parse(
    """
      |{
      |  "disability":
      |    {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "amountDeducted": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  private val invalidShareOptionJson: JsValue = Json.parse(
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
      |        "amountOfConsiderationReceived": -23122.22,
      |        "noOfSharesAcquired": 1,
      |        "classOfSharesAcquired": "FIRST",
      |        "exercisePrice": 12.22,
      |        "amountPaidForOption": 123.22,
      |        "marketValueOfSharesOnExcise": 1232.22,
      |        "profitOnOptionExercised": 1232.33,
      |        "employersNicPaid": 2312.22,
      |        "taxableAmount" : 2132.22
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  private def invalidDatesInSharesAwardedOrReceivedJson(dateSharesCeasedToBeSubjectToPlan: String, dateSharesAwarded: String): JsValue = Json.parse(
    s"""
       |{
       |  "sharesAwardedOrReceived": [
       |     {
       |        "employerName": "Company Ltd",
       |        "employerRef" : "123/AB456",
       |        "schemePlanType": "sip",
       |        "dateSharesCeasedToBeSubjectToPlan": "$dateSharesCeasedToBeSubjectToPlan",
       |        "noOfShareSecuritiesAwarded": 11,
       |        "classOfShareAwarded": "FIRST",
       |        "dateSharesAwarded" : "$dateSharesAwarded",
       |        "sharesSubjectToRestrictions": true,
       |        "electionEnteredIgnoreRestrictions": false,
       |        "actualMarketValueOfSharesOnAward": 2123.22,
       |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
       |        "amountPaidForSharesOnAward": 123.22,
       |        "marketValueAfterRestrictionsLifted": 1232.22,
       |        "taxableAmount": 12321.22
       |      }
       |   ]
       |}
       |""".stripMargin
  )

  private val invalidSharesAwardedOrReceivedJson: JsValue = Json.parse(
    """
      |{
      |  "sharesAwardedOrReceived": [
      |     {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "123/AB456",
      |        "schemePlanType": "sip",
      |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |        "noOfShareSecuritiesAwarded": 11,
      |        "classOfShareAwarded": "FIRST",
      |        "dateSharesAwarded" : "2019-11-20",
      |        "sharesSubjectToRestrictions": true,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": -2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |        "amountPaidForSharesOnAward": 123.22,
      |        "marketValueAfterRestrictionsLifted": 1232.22,
      |        "taxableAmount": 12321.22
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  private val invalidDisabilityJson: JsValue = Json.parse(
    """
      |{
      |  "disability":
      |    {
      |      "customerReference": "OTHEREmp123A",
      |      "amountDeducted": 5000.999
      |    }
      |}
      |""".stripMargin
  )

  private val invalidForeignServiceJson: JsValue = Json.parse(
    """
      |{
      |  "foreignService":
      |    {
      |      "customerReference": "OTHEREmp123A",
      |      "amountDeducted": -5000.99
      |    }
      |}
      |""".stripMargin
  )

  private val invalidLumpSumsJson: JsValue = Json.parse(
    """
      |{
      |  "lumpSums": [
      |    {
      |      "employerName": "Company Ltd",
      |      "employerRef" : "123/AB456",
      |      "taxableLumpSumsAndCertainIncome":
      |         {
      |           "amount": -1111.11,
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
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val ruleLumpSumsSingleErrorScenarioJson: JsValue = Json.parse(
    """
      |{
      |  "lumpSums": [
      |    {
      |      "employerName": "name",
      |      "employerRef": "456/AB452",
      |      "taxableLumpSumsAndCertainIncome": {
      |        "amount": 100.11,
      |        "taxTakenOffInEmployment": true
      |      }
      |    },
      |    {
      |      "employerName": "name",
      |      "employerRef": "456/AB456"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val ruleLumpSumsMultipleErrorScenarioJson: JsValue = Json.parse(
    """
      |{
      |  "lumpSums": [
      |    {
      |      "employerName": "name",
      |      "employerRef": "123/AB456"
      |    },
      |    {
      |      "employerName": "name",
      |      "employerRef": "123/AB457",
      |      "taxableLumpSumsAndCertainIncome": {
      |        "amount": 100.11,
      |        "taxTakenOffInEmployment": true
      |      }
      |    },
      |    {
      |      "employerName": "name2",
      |      "employerRef": "123/AB458"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |        "employerRef" : "InvalidReference",
      |        "schemePlanType": "NotAScheme",
      |        "dateOfOptionGrant": "19-11-20",
      |        "dateOfEvent": "19-11-20",
      |        "optionNotExercisedButConsiderationReceived": false,
      |        "amountOfConsiderationReceived": -23122.22,
      |        "noOfSharesAcquired": -100,
      |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
      |        "exercisePrice": 12.222,
      |        "amountPaidForOption": -123.22,
      |        "marketValueOfSharesOnExcise": -1232.222,
      |        "profitOnOptionExercised": -1232.33,
      |        "employersNicPaid": 2312.222,
      |        "taxableAmount" : -2132.222
      |      },
      |      {
      |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |        "employerRef" : "InvalidReference",
      |        "schemePlanType": "NotAScheme",
      |        "dateOfOptionGrant": "20-9-12",
      |        "dateOfEvent": "20-9-12",
      |        "optionNotExercisedButConsiderationReceived": false,
      |        "amountOfConsiderationReceived": 5000.999,
      |        "noOfSharesAcquired": -200,
      |        "classOfSharesAcquired": "",
      |        "exercisePrice": -1000.99,
      |        "amountPaidForOption": 5000.999,
      |        "marketValueOfSharesOnExcise": -5500.999,
      |        "profitOnOptionExercised": -3333.333,
      |        "employersNicPaid": 2000.222,
      |        "taxableAmount" : -2555.55
      |      }
      |  ],
      |  "sharesAwardedOrReceived": [
      |       {
      |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |        "employerRef" : "InvalidReference",
      |        "schemePlanType": "NotAScheme",
      |        "dateSharesCeasedToBeSubjectToPlan": "19-11-10",
      |        "noOfShareSecuritiesAwarded": -11,
      |        "classOfShareAwarded": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
      |        "dateSharesAwarded" : "19-11-20",
      |        "sharesSubjectToRestrictions": false,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": -2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.222,
      |        "amountPaidForSharesOnAward": -123.222,
      |        "marketValueAfterRestrictionsLifted": -1232.222,
      |        "taxableAmount": -12321.22
      |       },
      |       {
      |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |        "employerRef" : "InvalidReference",
      |        "schemePlanType": "NotAScheme",
      |        "dateSharesCeasedToBeSubjectToPlan": "20-9-12",
      |        "noOfShareSecuritiesAwarded": -299,
      |        "classOfShareAwarded": "",
      |        "dateSharesAwarded" : "20-09-12",
      |        "sharesSubjectToRestrictions": false,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": -5000.999,
      |        "unrestrictedMarketValueOfSharesOnAward": -5432.21,
      |        "amountPaidForSharesOnAward": -6000.99,
      |        "marketValueAfterRestrictionsLifted": -3333.333,
      |        "taxableAmount": 98765.999
      |       }
      |  ],
      |  "disability":
      |    {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "amountDeducted": -5000.99
      |    },
      |  "foreignService":
      |    {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "amountDeducted": 7000.999
      |    },
      |   "lumpSums": [
      |    {
      |      "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |      "employerRef" : "InvalidReference",
      |      "taxableLumpSumsAndCertainIncome":
      |         {
      |           "amount": -1111.11,
      |           "taxPaid": 3333.333,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "benefitFromEmployerFinancedRetirementScheme":
      |         {
      |           "amount": -1000.999,
      |           "exemptAmount": 2345.999,
      |           "taxPaid": 123.456,
      |           "taxTakenOffInEmployment": false
      |         },
      |      "redundancyCompensationPaymentsOverExemption":
      |         {
      |           "amount": -2000.22,
      |           "taxPaid": 3333.333,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsUnderExemption":
      |         {
      |           "amount": -5000.99
      |         }
      |      },
      |      {
      |      "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
      |      "employerRef" : "InvalidReference",
      |      "taxableLumpSumsAndCertainIncome":
      |         {
      |           "amount": 9999.999,
      |           "taxPaid": 2727.211,
      |           "taxTakenOffInEmployment": false
      |         },
      |      "benefitFromEmployerFinancedRetirementScheme":
      |         {
      |           "amount": -1000.999,
      |           "exemptAmount": -2345.11,
      |           "taxPaid": 5000.555,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsOverExemption":
      |         {
      |           "amount": -2000.22,
      |           "taxPaid": 3333.333,
      |           "taxTakenOffInEmployment": false
      |         },
      |      "redundancyCompensationPaymentsUnderExemption":
      |         {
      |           "amount": 5000.999
      |         }
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody    = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody    = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody = AnyContentAsJson(nonValidRequestBodyJson)

  private val invalidEmployerNameRawRequestBody          = AnyContentAsJson(invalidEmployerNameJson)
  private val invalidEmployerRefRawRequestBody           = AnyContentAsJson(invalidEmployerRefJson)
  private val invalidSchemePlanRawRequestBody            = AnyContentAsJson(invalidSchemePlanTypeJson)
  private val invalidDateRawRequestBody                  = AnyContentAsJson(invalidDateJson)
  private val invalidClassOfSharesAcquiredRawRequestBody = AnyContentAsJson(invalidClassOfSharesAcquiredJson)
  private val invalidClassOfSharesAwardedRawRequestBody  = AnyContentAsJson(invalidClassOfSharesAwardedJson)
  private val invalidCustomerRefRawRequestBody           = AnyContentAsJson(invalidCustomerRefJson)

  private val invalidShareOptionRawRequestBody             = AnyContentAsJson(invalidShareOptionJson)
  private val invalidSharesAwardedOrReceivedRawRequestBody = AnyContentAsJson(invalidSharesAwardedOrReceivedJson)
  private val invalidDisabilityRawRequestBody              = AnyContentAsJson(invalidDisabilityJson)
  private val invalidForeignServiceRawRequestBody          = AnyContentAsJson(invalidForeignServiceJson)
  private val invalidLumpSumsRawRequestBody                = AnyContentAsJson(invalidLumpSumsJson)
  private val ruleLumpSumsSingleScenarioRawRequestBody     = AnyContentAsJson(ruleLumpSumsSingleErrorScenarioJson)
  private val ruleLumpSumsMultipleScenarioRawRequestBody   = AnyContentAsJson(ruleLumpSumsMultipleErrorScenarioJson)
  private val allInvalidValueRawRequestBody                = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendOtherEmploymentValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendOtherEmploymentRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
      validator.validate(AmendOtherEmploymentRawData(validNino, "2020-22", validRawRequestBody)) shouldBe
        List(RuleTaxYearRangeInvalidError)
    }

    "return multiple errors for multiple invalid request parameters" in new Test {
      validator.validate(AmendOtherEmploymentRawData("notValid", "2020-22", validRawRequestBody)) shouldBe
        List(NinoFormatError, RuleTaxYearRangeInvalidError)
    }

    // parameter rule error scenarios
    "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
      validator.validate(AmendOtherEmploymentRawData(validNino, "2019-20", validRawRequestBody)) shouldBe
        List(RuleTaxYearNotSupportedError)
    }

    // body format error scenarios
    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
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

        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }
    }

    "return EmployerNameFormatError error" when {
      "an incorrectly formatted employer name is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidEmployerNameRawRequestBody)) shouldBe
          List(EmployerNameFormatError.copy(paths = Some(List("/shareOption/0/employerName"))))
      }
    }

    "return EmployerRefFormatError error" when {
      "an incorrectly formatted employer reference is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidEmployerRefRawRequestBody)) shouldBe
          List(EmployerRefFormatError.copy(paths = Some(List("/shareOption/0/employerRef"))))
      }
    }

    "return SchemePlanTypeFormatError error" when {
      "an incorrectly formatted scheme plan type is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidSchemePlanRawRequestBody)) shouldBe
          List(SchemePlanTypeFormatError.copy(paths = Some(List("/shareOption/0/schemePlanType"))))
      }
    }

    "return DateFormatError error" when {
      "an incorrectly formatted date is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidDateRawRequestBody)) shouldBe
          List(DateFormatError.copy(paths = Some(List("/shareOption/0/dateOfOptionGrant"))))
      }
    }

    "return ClassOfSharesAcquiredFormatError error" when {
      "an incorrectly formatted class of shares type is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidClassOfSharesAcquiredRawRequestBody)) shouldBe
          List(ClassOfSharesAcquiredFormatError.copy(paths = Some(List("/shareOption/0/classOfSharesAcquired"))))
      }
    }

    "return ClassOfSharesAwardedFormatError error" when {
      "an incorrectly formatted class of shares type is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidClassOfSharesAwardedRawRequestBody)) shouldBe
          List(ClassOfSharesAwardedFormatError.copy(paths = Some(List("/sharesAwardedOrReceived/0/classOfShareAwarded"))))
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted class of shares type is submitted" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidCustomerRefRawRequestBody)) shouldBe
          List(CustomerRefFormatError.copy(paths = Some(List("/disability/customerReference"))))
      }
    }

    "return Date validation errors" when {
      "an out of range date (dateSharesCeasedToBeSubjectToPlan) is submitted" in new Test {
        val invalidDateSharesCeasedToBeSubjectToPlan =
          invalidDatesInSharesAwardedOrReceivedJson(dateSharesCeasedToBeSubjectToPlan = "0010-01-01", dateSharesAwarded = "2019-01-01")

        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, AnyContentAsJson(invalidDateSharesCeasedToBeSubjectToPlan))) shouldBe
          List(RuleDateRangeInvalidError.copy(paths = Some(List("/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan"))))
      }

      "an incorrectly formatted date (dateSharesCeasedToBeSubjectToPlan) is submitted" in new Test {
        val invalidDateSharesCeasedToBeSubjectToPlan =
          invalidDatesInSharesAwardedOrReceivedJson(dateSharesCeasedToBeSubjectToPlan = "19-01-01", dateSharesAwarded = "2019-01-01")

        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, AnyContentAsJson(invalidDateSharesCeasedToBeSubjectToPlan))) shouldBe
          List(DateFormatError.copy(paths = Some(List("/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan"))))
      }

      "an out of range date (dateSharesAwarded) is submitted" in new Test {
        val invalidDateSharesAwarded =
          invalidDatesInSharesAwardedOrReceivedJson(dateSharesCeasedToBeSubjectToPlan = "2019-01-01", dateSharesAwarded = "2101-01-01")

        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, AnyContentAsJson(invalidDateSharesAwarded))) shouldBe
          List(RuleDateRangeInvalidError.copy(paths = Some(List("/sharesAwardedOrReceived/0/dateSharesAwarded"))))
      }

      "an incorrectly formatted date (dateSharesAwarded) is submitted" in new Test {
        val invalidDateSharesAwarded =
          invalidDatesInSharesAwardedOrReceivedJson(dateSharesCeasedToBeSubjectToPlan = "2019-01-01", dateSharesAwarded = "21-01-01")

        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, AnyContentAsJson(invalidDateSharesAwarded))) shouldBe
          List(DateFormatError.copy(paths = Some(List("/sharesAwardedOrReceived/0/dateSharesAwarded"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (shareOption)" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidShareOptionRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/shareOption/0/amountOfConsiderationReceived"))
            ))
      }

      "one field fails value validation (sharesAwardedOrReceived)" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidSharesAwardedOrReceivedRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward"))
            ))
      }

      "one field fails value validation (Disability)" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidDisabilityRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/disability/amountDeducted"))
            ))
      }

      "one field fails value validation (foreignService)" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidForeignServiceRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/foreignService/amountDeducted"))
            ))
      }

      "one field fails value validation (lumpSums)" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, invalidLumpSumsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/lumpSums/0/taxableLumpSumsAndCertainIncome/amount"))
            ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendOtherEmploymentRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            ClassOfSharesAcquiredFormatError.copy(
              paths = Some(
                List(
                  "/shareOption/0/classOfSharesAcquired",
                  "/shareOption/1/classOfSharesAcquired"
                ))
            ),
            ClassOfSharesAwardedFormatError.copy(
              paths = Some(
                List(
                  "/sharesAwardedOrReceived/0/classOfShareAwarded",
                  "/sharesAwardedOrReceived/1/classOfShareAwarded"
                ))
            ),
            CustomerRefFormatError.copy(
              paths = Some(
                List(
                  "/disability/customerReference",
                  "/foreignService/customerReference"
                ))
            ),
            DateFormatError.copy(
              paths = Some(List(
                "/shareOption/0/dateOfOptionGrant",
                "/shareOption/0/dateOfEvent",
                "/shareOption/1/dateOfOptionGrant",
                "/shareOption/1/dateOfEvent",
                "/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan",
                "/sharesAwardedOrReceived/0/dateSharesAwarded",
                "/sharesAwardedOrReceived/1/dateSharesCeasedToBeSubjectToPlan",
                "/sharesAwardedOrReceived/1/dateSharesAwarded"
              ))
            ),
            EmployerNameFormatError.copy(
              paths = Some(List(
                "/shareOption/0/employerName",
                "/shareOption/1/employerName",
                "/sharesAwardedOrReceived/0/employerName",
                "/sharesAwardedOrReceived/1/employerName",
                "/lumpSums/0/employerName",
                "/lumpSums/1/employerName"
              ))
            ),
            EmployerRefFormatError.copy(
              paths = Some(List(
                "/shareOption/0/employerRef",
                "/shareOption/1/employerRef",
                "/sharesAwardedOrReceived/0/employerRef",
                "/sharesAwardedOrReceived/1/employerRef",
                "/lumpSums/0/employerRef",
                "/lumpSums/1/employerRef"
              ))
            ),
            SchemePlanTypeFormatError.copy(
              paths = Some(List(
                "/shareOption/0/schemePlanType",
                "/shareOption/1/schemePlanType",
                "/sharesAwardedOrReceived/0/schemePlanType",
                "/sharesAwardedOrReceived/1/schemePlanType"
              ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_BIG_INTEGER_INCLUSIVE,
              paths = Some(List(
                "/shareOption/0/noOfSharesAcquired",
                "/shareOption/1/noOfSharesAcquired",
                "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded",
                "/sharesAwardedOrReceived/1/noOfShareSecuritiesAwarded"
              ))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/shareOption/0/amountOfConsiderationReceived",
                "/shareOption/0/exercisePrice",
                "/shareOption/0/amountPaidForOption",
                "/shareOption/0/marketValueOfSharesOnExcise",
                "/shareOption/0/profitOnOptionExercised",
                "/shareOption/0/employersNicPaid",
                "/shareOption/0/taxableAmount",
                "/shareOption/1/amountOfConsiderationReceived",
                "/shareOption/1/exercisePrice",
                "/shareOption/1/amountPaidForOption",
                "/shareOption/1/marketValueOfSharesOnExcise",
                "/shareOption/1/profitOnOptionExercised",
                "/shareOption/1/employersNicPaid",
                "/shareOption/1/taxableAmount",
                "/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward",
                "/sharesAwardedOrReceived/0/unrestrictedMarketValueOfSharesOnAward",
                "/sharesAwardedOrReceived/0/amountPaidForSharesOnAward",
                "/sharesAwardedOrReceived/0/marketValueAfterRestrictionsLifted",
                "/sharesAwardedOrReceived/0/taxableAmount",
                "/sharesAwardedOrReceived/1/actualMarketValueOfSharesOnAward",
                "/sharesAwardedOrReceived/1/unrestrictedMarketValueOfSharesOnAward",
                "/sharesAwardedOrReceived/1/amountPaidForSharesOnAward",
                "/sharesAwardedOrReceived/1/marketValueAfterRestrictionsLifted",
                "/sharesAwardedOrReceived/1/taxableAmount",
                "/disability/amountDeducted",
                "/foreignService/amountDeducted",
                "/lumpSums/0/taxableLumpSumsAndCertainIncome/amount",
                "/lumpSums/0/taxableLumpSumsAndCertainIncome/taxPaid",
                "/lumpSums/0/benefitFromEmployerFinancedRetirementSchemeItem/amount",
                "/lumpSums/0/benefitFromEmployerFinancedRetirementSchemeItem/exemptAmount",
                "/lumpSums/0/benefitFromEmployerFinancedRetirementSchemeItem/taxPaid",
                "/lumpSums/0/redundancyCompensationPaymentsOverExemptionItem/amount",
                "/lumpSums/0/redundancyCompensationPaymentsOverExemptionItem/taxPaid",
                "/lumpSums/0/redundancyCompensationPaymentsUnderExemptionItem/amount",
                "/lumpSums/1/taxableLumpSumsAndCertainIncome/amount",
                "/lumpSums/1/taxableLumpSumsAndCertainIncome/taxPaid",
                "/lumpSums/1/benefitFromEmployerFinancedRetirementSchemeItem/amount",
                "/lumpSums/1/benefitFromEmployerFinancedRetirementSchemeItem/exemptAmount",
                "/lumpSums/1/benefitFromEmployerFinancedRetirementSchemeItem/taxPaid",
                "/lumpSums/1/redundancyCompensationPaymentsOverExemptionItem/amount",
                "/lumpSums/1/redundancyCompensationPaymentsOverExemptionItem/taxPaid",
                "/lumpSums/1/redundancyCompensationPaymentsUnderExemptionItem/amount"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendOtherEmploymentRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return RuleLumpSumsError error" when {
      "a lumpSums array item has been supplied without any defined child objects" in new Test {
        validator.validate(AmendOtherEmploymentRawData("AA111111A", "2020-21", ruleLumpSumsSingleScenarioRawRequestBody)) shouldBe
          List(RuleLumpSumsError.copy(paths = Some(Seq("/lumpSums/1"))))
      }

      "multiple lumpSums array items have been supplied without any defined child objects" in new Test {
        validator.validate(AmendOtherEmploymentRawData("AA111111A", "2020-21", ruleLumpSumsMultipleScenarioRawRequestBody)) shouldBe
          List(RuleLumpSumsError.copy(paths = Some(Seq("/lumpSums/0", "/lumpSums/2"))))
      }
    }
  }

}
