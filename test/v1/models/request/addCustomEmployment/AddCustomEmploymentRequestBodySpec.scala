/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.models.request.addCustomEmployment

import play.api.Configuration
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.utils.UnitSpec

class AddCustomEmploymentRequestBodySpec extends UnitSpec with MockSharedAppConfig {

  private class Test(isHipEnabled: Boolean) {
    MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("ifs_hip_migration_1661.enabled" -> isHipEnabled))
  }

  "AddCustomEmploymentRequestBody" when {
    def json(payrollId: String = "124214112412"): JsValue = Json.parse(
      s"""
        |{
        |  "employerRef": "123/AB56797",
        |  "employerName": "AMD infotech Ltd",
        |  "startDate": "2019-01-01",
        |  "cessationDate": "2020-06-01",
        |  "payrollId": "$payrollId",
        |  "occupationalPension": false
        |}
      """.stripMargin
    )

    val model: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
      employerRef = Some("123/AB56797"),
      employerName = "AMD infotech Ltd",
      startDate = "2019-01-01",
      cessationDate = Some("2020-06-01"),
      payrollId = Some("124214112412"),
      occupationalPension = false
    )

    "read from valid JSON" should {
      "produce the expected AddCustomEmploymentRequestBody object" in {
        json().as[AddCustomEmploymentRequestBody] shouldBe model
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "employerRef": "123/AB56797",
            |  "cessationDate": "2020-06-01",
            |  "payrollId": "124214112412",
            |  "occupationalPension": false
            |}
          """.stripMargin
        )

        json.validate[AddCustomEmploymentRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" when {
      "the feature switch is disabled (IFS enabled)" should {
        "produce the expected JsObject without truncating payrollId" in new Test(false) {
          val updatedModel: AddCustomEmploymentRequestBody = model.copy(payrollId = Some("a" * 38))

          Json.toJson(updatedModel) shouldBe json("a" * 38)
        }

        "produce the expected JsObject with # stripped from payrollId" in new Test(false) {
          val updatedModel: AddCustomEmploymentRequestBody = model.copy(payrollId = Some("ABC#123456789"))

          Json.toJson(updatedModel) shouldBe json("ABC123456789")
        }
      }

      "the feature switch is enabled (HIP enabled)" should {
        "produce the expected JsObject without truncating payrollId" when {
          "it is within the 35 character limit" in new Test(true) {
            Json.toJson(model) shouldBe json()
          }
        }

        "produce the expected JsObject with payrollId truncated to 35 characters" when {
          "it exceeds the 35 character limit" in new Test(true) {
            val updatedModel: AddCustomEmploymentRequestBody = model.copy(payrollId = Some("a" * 38))

            Json.toJson(updatedModel) shouldBe json("a" * 35)
          }
        }
      }

      "payrollId is not provided" should {
        "produce the expected JsObject without payrollId" in {
          val updatedModel: AddCustomEmploymentRequestBody = model.copy(payrollId = None)

          Json.toJson(updatedModel) shouldBe json().as[JsObject] - "payrollId"
        }
      }
    }
  }

}
