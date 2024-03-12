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

package v1.models.response.retrieveOtherEmployment

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class RedundancyCompensationPaymentsOverExemptionItemSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      | "amount": 5000.99,
      | "taxPaid": 3333.33,
      | "taxTakenOffInEmployment": true
      |}
      |""".stripMargin
  )

  private val model = RedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  "RedundancyCompensationPaymentsOverExemptionItem" when {
    "read from valid JSON" should {
      "produce the expected RedundancyCompensationPaymentsOverExemptionItem object" in {
        json.as[RedundancyCompensationPaymentsOverExemptionItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[RedundancyCompensationPaymentsOverExemptionItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}