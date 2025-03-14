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

package v2.models.response.retrieveOtherEmployment

import common.models.domain.SharesAwardedOrReceivedSchemeType
import play.api.libs.json.{JsError, JsObject, Json}
import shared.utils.UnitSpec

class SharesAwardedOrReceivedItemSpec extends UnitSpec {

  private val model = SharesAwardedOrReceivedItem(
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

  "SharesAwardedOrReceivedItem" when {
    "read from valid JSON" should {
      "produce the expected SharesAwardedOrReceivedItem object" in {
        Json
          .parse(
            """
            |{
            |   "employerName": "Company Ltd",
            |   "employerRef" : "123/AB456",
            |   "schemePlanType": "SIP",
            |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
            |   "noOfShareSecuritiesAwarded": 11,
            |   "classOfShareAwarded": "FIRST",
            |   "dateSharesAwarded" : "2019-12-20",
            |   "sharesSubjectToRestrictions": true,
            |   "electionEnteredIgnoreRestrictions": false,
            |   "actualMarketValueOfSharesOnAward": 2123.22,
            |   "unrestrictedMarketValueOfSharesOnAward": 123.22,
            |   "amountPaidForSharesOnAward": 123.22,
            |   "marketValueAfterRestrictionsLifted": 1232.22,
            |   "taxableAmount": 12321.22
            |}
            """.stripMargin
          )
          .as[SharesAwardedOrReceivedItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[SharesAwardedOrReceivedItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe Json.parse(
          """
            |{
            |   "employerName": "Company Ltd",
            |   "employerRef" : "123/AB456",
            |   "schemePlanType": "sip",
            |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
            |   "noOfShareSecuritiesAwarded": 11,
            |   "classOfShareAwarded": "FIRST",
            |   "dateSharesAwarded" : "2019-12-20",
            |   "sharesSubjectToRestrictions": true,
            |   "electionEnteredIgnoreRestrictions": false,
            |   "actualMarketValueOfSharesOnAward": 2123.22,
            |   "unrestrictedMarketValueOfSharesOnAward": 123.22,
            |   "amountPaidForSharesOnAward": 123.22,
            |   "marketValueAfterRestrictionsLifted": 1232.22,
            |   "taxableAmount": 12321.22
            |}
            """.stripMargin
        )
      }
    }
  }

}
