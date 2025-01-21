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

package v2.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveEmploymentControllerFixture {

  val hmrcEnteredResponseWithoutDateIgnored: JsValue = Json.parse(
    """
      |{
      |   "employments": {
      |      "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |      "employerName": "Employer Name Ltd.",
      |      "employerRef": "123/AB56797",
      |      "payrollId": "123345657",
      |      "occupationalPension": false,
      |      "startDate": "2020-06-17",
      |      "cessationDate": "2020-06-17"
      |   }
      |}
    """.stripMargin
  )

  val hmrcEnteredResponseWithDateIgnored: JsValue = Json.parse(
    """
      |{
      |   "employments": {
      |      "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |      "employerName": "Employer Name Ltd.",
      |      "employerRef": "123/AB56797",
      |      "payrollId": "123345657",
      |      "occupationalPension": false,
      |      "startDate": "2020-06-17",
      |      "cessationDate": "2020-06-17",
      |      "dateIgnored": "2020-06-17T10:53:38Z"
      |   }
      |}
    """.stripMargin
  )

  val customEnteredResponse: JsValue = Json.parse(
    """
      |{
      |   "customerDeclaredEmployments": {
      |      "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |      "employerName": "Employer Name Ltd.",
      |      "employerRef": "123/AB56797",
      |      "payrollId": "123345657",
      |      "occupationalPension": false,
      |      "startDate": "2020-06-17",
      |      "cessationDate": "2020-06-17",
      |      "submittedOn": "2020-06-17T10:53:38Z"
      |   }
      |}
    """.stripMargin
  )

  val mtdHmrcEnteredResponseWithNoDateIgnored: JsObject = Json
    .parse(
      s"""
      |{
      |   "employerRef": "123/AB56797",
      |   "employerName": "Employer Name Ltd.",
      |   "startDate": "2020-06-17",
      |   "cessationDate": "2020-06-17",
      |   "payrollId": "123345657",
      |   "occupationalPension": false
      |}
      |""".stripMargin
    )
    .as[JsObject]

  val mtdHmrcEnteredResponseWithDateIgnored: JsObject = Json
    .parse(
      s"""
       |{
       |   "employerRef": "123/AB56797",
       |   "employerName": "Employer Name Ltd.",
       |   "startDate": "2020-06-17",
       |   "cessationDate": "2020-06-17",
       |   "payrollId": "123345657",
       |   "occupationalPension": false,
       |   "dateIgnored": "2020-06-17T10:53:38.000Z"
       |}
       |""".stripMargin
    )
    .as[JsObject]

  val mtdCustomEnteredResponse: JsObject = Json
    .parse(
      s"""
       |{
       |   "employerRef": "123/AB56797",
       |   "employerName": "Employer Name Ltd.",
       |   "startDate": "2020-06-17",
       |   "cessationDate": "2020-06-17",
       |   "payrollId": "123345657",
       |   "occupationalPension": false,
       |   "submittedOn": "2020-06-17T10:53:38.000Z"
       |}
       |""".stripMargin
    )
    .as[JsObject]

}
