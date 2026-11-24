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

package v2.retrieveOtherEmploymentIncome

import api.models.domain.Timestamp
import common.models.domain.{ShareOptionSchemeType, SharesAwardedOrReceivedSchemeType}
import play.api.libs.json.{JsValue, Json}
import v2.retrieveOtherEmploymentIncome.model.response.*

object RetrieveOtherEmploymentIncomeFixtures {

  val downstreamResponse: JsValue = Json.parse(
    """
      |{
      | "submittedOn": "2020-07-06T09:37:17.000Z",
      | "shareOption":[
      |   {
      |      "employerName": "Company Ltd",
      |      "employerRef" : "123/AB456",
      |      "schemePlanType": "EMI",
      |      "dateOfOptionGrant": "2019-11-20",
      |      "dateOfEvent": "2019-12-22",
      |      "optionNotExercisedButConsiderationReceived": true,
      |      "amountOfConsiderationReceived": 23122.22,
      |      "noOfSharesAcquired": 1,
      |      "classOfSharesAcquired": "FIRST",
      |      "exercisePrice": 12.22,
      |      "amountPaidForOption": 123.22,
      |      "marketValueOfSharesOnExcise": 1232.22,
      |      "profitOnOptionExercised": 1232.33,
      |      "employersNicPaid": 2312.22,
      |      "taxableAmount" : 2132.22
      |   }
      | ],
      | "sharesAwardedOrReceived":[
      |   {
      |     "employerName": "Company Ltd",
      |     "employerRef" : "123/AB456",
      |     "schemePlanType": "SIP",
      |     "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |     "noOfShareSecuritiesAwarded": 11,
      |     "classOfShareAwarded": "FIRST",
      |     "dateSharesAwarded" : "2019-12-20",
      |     "sharesSubjectToRestrictions": true,
      |     "electionEnteredIgnoreRestrictions": false,
      |     "actualMarketValueOfSharesOnAward": 2123.22,
      |     "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |     "amountPaidForSharesOnAward": 123.22,
      |     "marketValueAfterRestrictionsLifted": 1232.22,
      |     "taxableAmount": 12321.22
      |   }
      | ],
      | "disability":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "foreignService":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "lumpSums": [
      |    {
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
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      | "submittedOn": "2020-07-06T09:37:17.000Z",
      | "shareOption":[
      |   {
      |      "employerName": "Company Ltd",
      |      "employerRef" : "123/AB456",
      |      "schemePlanType": "emi",
      |      "dateOfOptionGrant": "2019-11-20",
      |      "dateOfEvent": "2019-12-22",
      |      "optionNotExercisedButConsiderationReceived": true,
      |      "amountOfConsiderationReceived": 23122.22,
      |      "noOfSharesAcquired": 1,
      |      "classOfSharesAcquired": "FIRST",
      |      "exercisePrice": 12.22,
      |      "amountPaidForOption": 123.22,
      |      "marketValueOfSharesOnExcise": 1232.22,
      |      "profitOnOptionExercised": 1232.33,
      |      "employersNicPaid": 2312.22,
      |      "taxableAmount" : 2132.22
      |   }
      | ],
      | "sharesAwardedOrReceived":[
      |   {
      |     "employerName": "Company Ltd",
      |     "employerRef" : "123/AB456",
      |     "schemePlanType": "sip",
      |     "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |     "noOfShareSecuritiesAwarded": 11,
      |     "classOfShareAwarded": "FIRST",
      |     "dateSharesAwarded" : "2019-12-20",
      |     "sharesSubjectToRestrictions": true,
      |     "electionEnteredIgnoreRestrictions": false,
      |     "actualMarketValueOfSharesOnAward": 2123.22,
      |     "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |     "amountPaidForSharesOnAward": 123.22,
      |     "marketValueAfterRestrictionsLifted": 1232.22,
      |     "taxableAmount": 12321.22
      |   }
      | ],
      | "disability":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "foreignService":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "lumpSums": [
      |    {
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
      |""".stripMargin
  )

  val shareOption: ShareOptionItem = ShareOptionItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = ShareOptionSchemeType.`emi`,
    dateOfOptionGrant = "2019-11-20",
    dateOfEvent = "2019-12-22",
    optionNotExercisedButConsiderationReceived = Some(true),
    amountOfConsiderationReceived = 23122.22,
    noOfSharesAcquired = 1,
    classOfSharesAcquired = Some("FIRST"),
    exercisePrice = 12.22,
    amountPaidForOption = 123.22,
    marketValueOfSharesOnExcise = 1232.22,
    profitOnOptionExercised = 1232.33,
    employersNicPaid = 2312.22,
    taxableAmount = 2132.22
  )

  val sharesAwardedOrReceived: SharesAwardedOrReceivedItem = SharesAwardedOrReceivedItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = SharesAwardedOrReceivedSchemeType.`sip`,
    dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
    noOfShareSecuritiesAwarded = 11,
    classOfShareAwarded = "FIRST",
    dateSharesAwarded = "2019-12-20",
    sharesSubjectToRestrictions = true,
    electionEnteredIgnoreRestrictions = false,
    actualMarketValueOfSharesOnAward = 2123.22,
    unrestrictedMarketValueOfSharesOnAward = 123.22,
    amountPaidForSharesOnAward = 123.22,
    marketValueAfterRestrictionsLifted = 1232.22,
    taxableAmount = 12321.22
  )

  val commonOtherEmployment: CommonOtherEmployment = CommonOtherEmployment(
    customerReference = Some("customer reference"),
    amountDeducted = 1223.22
  )

  val benefitFromEmployerFinancedRetirementScheme: BenefitFromEmployerFinancedRetirementSchemeItem = BenefitFromEmployerFinancedRetirementSchemeItem(
    amount = 5000.99,
    exemptAmount = Some(2345.99),
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = Some(true)
  )

  val taxableLumpSumsAndCertainIncome: TaxableLumpSumsAndCertainIncomeItem = TaxableLumpSumsAndCertainIncomeItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = Some(true)
  )

  val redundancyCompensationPaymentsOverExemption: RedundancyCompensationPaymentsOverExemptionItem = RedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = Some(true)
  )

  val redundancyCompensationPaymentsUnderExemption: RedundancyCompensationPaymentsUnderExemptionItem =
    RedundancyCompensationPaymentsUnderExemptionItem(
      amount = 5000.99
    )

  val lumpSums: LumpSums = LumpSums(
    employerName = "BPDTS Ltd",
    employerRef = "123/AB456",
    taxableLumpSumsAndCertainIncome = Some(taxableLumpSumsAndCertainIncome),
    benefitFromEmployerFinancedRetirementScheme = Some(benefitFromEmployerFinancedRetirementScheme),
    redundancyCompensationPaymentsOverExemption = Some(redundancyCompensationPaymentsOverExemption),
    redundancyCompensationPaymentsUnderExemption = Some(redundancyCompensationPaymentsUnderExemption)
  )

  val retrieveResponse: RetrieveOtherEmploymentIncomeResponse = RetrieveOtherEmploymentIncomeResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    shareOption = Some(Seq(shareOption)),
    sharesAwardedOrReceived = Some(Seq(sharesAwardedOrReceived)),
    disability = Some(commonOtherEmployment),
    foreignService = Some(commonOtherEmployment),
    lumpSums = Some(Seq(lumpSums))
  )

  val retrieveOtherResponseModel: RetrieveOtherEmploymentIncomeResponse = RetrieveOtherEmploymentIncomeResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    shareOption = Some(Seq(shareOption)),
    sharesAwardedOrReceived = Some(Seq(sharesAwardedOrReceived)),
    disability = Some(commonOtherEmployment),
    foreignService = Some(commonOtherEmployment),
    lumpSums = Some(Seq(lumpSums))
  )

}
