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

package v1.models.request.amendOtherEmployment

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class AmendShareOptionItemSpec extends UnitSpec {

  private def modelWith(employerRef: Option[String]) = AmendShareOptionItem(
    employerName = "Company Ltd",
    employerRef = employerRef,
    schemePlanType = "emi",
    dateOfOptionGrant = "2001-01-01",
    dateOfEvent = "2002-02-02",
    optionNotExercisedButConsiderationReceived = true,
    amountOfConsiderationReceived = 1.01,
    noOfSharesAcquired = 2,
    classOfSharesAcquired = "FIRST",
    exercisePrice = 3.01,
    amountPaidForOption = 4.01,
    marketValueOfSharesOnExcise = 5.01,
    profitOnOptionExercised = 6.01,
    employersNicPaid = 7.01,
    taxableAmount = 8.01
  )

  private val model = modelWith(Some("AB1321/123"))

  "AmendShareOptionItem" when {
    "read from valid JSON" should {
      "produce the expected AmendShareOptionItem object" in {
        Json
          .parse(
            """
            |{
            |  "employerName": "Company Ltd",
            |  "employerRef" : "AB1321/123",
            |  "schemePlanType": "emi",
            |  "dateOfOptionGrant": "2001-01-01",
            |  "dateOfEvent": "2002-02-02",
            |  "optionNotExercisedButConsiderationReceived": true,
            |  "amountOfConsiderationReceived": 1.01,
            |  "noOfSharesAcquired": 2,
            |  "classOfSharesAcquired": "FIRST",
            |  "exercisePrice": 3.01,
            |  "amountPaidForOption": 4.01,
            |  "marketValueOfSharesOnExcise": 5.01,
            |  "profitOnOptionExercised": 6.01,
            |  "employersNicPaid": 7.01,
            |  "taxableAmount" : 8.01
            |}
            """.stripMargin
          )
          .as[AmendShareOptionItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[AmendShareOptionItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe Json.parse(
          """
            |{
            |  "employerName": "Company Ltd",
            |  "employerRef" : "AB1321/123",
            |  "schemePlanType": "EMI",
            |  "dateOfOptionGrant": "2001-01-01",
            |  "dateOfEvent": "2002-02-02",
            |  "optionNotExercisedButConsiderationReceived": true,
            |  "amountOfConsiderationReceived": 1.01,
            |  "noOfSharesAcquired": 2,
            |  "classOfSharesAcquired": "FIRST",
            |  "exercisePrice": 3.01,
            |  "amountPaidForOption": 4.01,
            |  "marketValueOfSharesOnExcise": 5.01,
            |  "profitOnOptionExercised": 6.01,
            |  "employersNicPaid": 7.01,
            |  "taxableAmount" : 8.01
            |}
            """.stripMargin
        )
      }

      "produce the expected JsObject when only mandatory fields present" in {
        Json.toJson(modelWith(employerRef = None)) shouldBe Json.parse(
          """
            |{
            |  "employerName": "Company Ltd",
            |  "schemePlanType": "EMI",
            |  "dateOfOptionGrant": "2001-01-01",
            |  "dateOfEvent": "2002-02-02",
            |  "optionNotExercisedButConsiderationReceived": true,
            |  "amountOfConsiderationReceived": 1.01,
            |  "noOfSharesAcquired": 2,
            |  "classOfSharesAcquired": "FIRST",
            |  "exercisePrice": 3.01,
            |  "amountPaidForOption": 4.01,
            |  "marketValueOfSharesOnExcise": 5.01,
            |  "profitOnOptionExercised": 6.01,
            |  "employersNicPaid": 7.01,
            |  "taxableAmount" : 8.01
            |}
            """.stripMargin
        )
      }
    }
  }

}
