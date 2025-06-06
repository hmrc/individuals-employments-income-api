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

import common.models.domain.MtdSourceEnum
import shared.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveNonPayeEmploymentIncome.{NonPayeIncome, RetrieveNonPayeEmploymentIncomeResponse}

object RetrieveNonPayeEmploymentControllerFixture {

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      | "submittedOn": "2020-07-06T09:37:17.000Z",
      | "source": "hmrc-held",
      | "totalNonPayeIncome": 23122.22,
      | "nonPayeIncome": {
      |   "tips": 2132.22
      | }
      |}
      |""".stripMargin
  )

  val downstreamResponse: JsValue = Json.parse(
    """
      |{
      | "submittedOn": "2020-07-06T09:37:17Z",
      | "source": "HMRC HELD",
      | "totalNonPayeIncome": 23122.22,
      | "nonPayeIncome": {
      |   "tips": 2132.22
      | }
      |}
      |""".stripMargin
  )

  val responseModel: RetrieveNonPayeEmploymentIncomeResponse = RetrieveNonPayeEmploymentIncomeResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    source = MtdSourceEnum.`hmrc-held`,
    totalNonPayeIncome = Some(23122.22),
    nonPayeIncome = Some(NonPayeIncome(tips = Some(2132.22)))
  )

}
