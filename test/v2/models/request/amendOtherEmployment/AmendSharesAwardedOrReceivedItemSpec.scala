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

package v2.models.request.amendOtherEmployment

import play.api.libs.json.{JsError, JsObject, Json}
import shared.utils.UnitSpec

class AmendSharesAwardedOrReceivedItemSpec extends UnitSpec {

  private def modelWith(employerRef: Option[String]) = AmendSharesAwardedOrReceivedItem(
    employerName = "Company Ltd",
    employerRef = employerRef,
    schemePlanType = "sip",
    dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
    noOfShareSecuritiesAwarded = 1,
    classOfShareAwarded = "FIRST",
    dateSharesAwarded = "2019-11-20",
    sharesSubjectToRestrictions = true,
    electionEnteredIgnoreRestrictions = false,
    actualMarketValueOfSharesOnAward = 2.01,
    unrestrictedMarketValueOfSharesOnAward = 3.01,
    amountPaidForSharesOnAward = 4.01,
    marketValueAfterRestrictionsLifted = 5.01,
    taxableAmount = 6.01
  )

  private val model = modelWith(Some("AB1321/123"))

  "AmendSharesAwardedOrReceivedItem" when {
    "read from valid JSON" should {
      "produce the expected AmendSharesAwardedOrReceivedItem object" in {
        Json
          .parse(
            """
            |{
            |   "employerName": "Company Ltd",
            |   "employerRef" : "AB1321/123",
            |   "schemePlanType": "sip",
            |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
            |   "noOfShareSecuritiesAwarded": 1,
            |   "classOfShareAwarded": "FIRST",
            |   "dateSharesAwarded" : "2019-11-20",
            |   "sharesSubjectToRestrictions": true,
            |   "electionEnteredIgnoreRestrictions": false,
            |   "actualMarketValueOfSharesOnAward": 2.01,
            |   "unrestrictedMarketValueOfSharesOnAward": 3.01,
            |   "amountPaidForSharesOnAward": 4.01,
            |   "marketValueAfterRestrictionsLifted": 5.01,
            |   "taxableAmount": 6.01
            |}
            """.stripMargin
          )
          .as[AmendSharesAwardedOrReceivedItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[AmendSharesAwardedOrReceivedItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe Json.parse(
          """
            |{
            |   "employerName": "Company Ltd",
            |   "employerRef" : "AB1321/123",
            |   "schemePlanType": "SIP",
            |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
            |   "noOfShareSecuritiesAwarded": 1,
            |   "classOfShareAwarded": "FIRST",
            |   "dateSharesAwarded" : "2019-11-20",
            |   "sharesSubjectToRestrictions": true,
            |   "electionEnteredIgnoreRestrictions": false,
            |   "actualMarketValueOfSharesOnAward": 2.01,
            |   "unrestrictedMarketValueOfSharesOnAward": 3.01,
            |   "amountPaidForSharesOnAward": 4.01,
            |   "marketValueAfterRestrictionsLifted": 5.01,
            |   "taxableAmount": 6.01
            |}
            """.stripMargin
        )
      }

      "produce the expected JsObject when only mandatory fields present" in {
        Json.toJson(modelWith(employerRef = None)) shouldBe Json.parse(
          """
            |{
            |   "employerName": "Company Ltd",
            |   "schemePlanType": "SIP",
            |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
            |   "noOfShareSecuritiesAwarded": 1,
            |   "classOfShareAwarded": "FIRST",
            |   "dateSharesAwarded" : "2019-11-20",
            |   "sharesSubjectToRestrictions": true,
            |   "electionEnteredIgnoreRestrictions": false,
            |   "actualMarketValueOfSharesOnAward": 2.01,
            |   "unrestrictedMarketValueOfSharesOnAward": 3.01,
            |   "amountPaidForSharesOnAward": 4.01,
            |   "marketValueAfterRestrictionsLifted": 5.01,
            |   "taxableAmount": 6.01
            |}
            """.stripMargin
        )
      }
    }
  }

}
