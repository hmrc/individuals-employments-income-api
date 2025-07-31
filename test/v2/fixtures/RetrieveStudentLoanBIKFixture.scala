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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import shared.models.domain.Timestamp
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse

object RetrieveStudentLoanBIKFixture {

  val downstreamResponse: JsValue = Json.parse(
  """
     |{
     | "submittedOn": "2025-08-24T14:15:22Z",
     | "payrolledBenefits": 20000.01
     |}
     |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
  """
      |{
      | "submittedOn": "2025-08-24T14:15:22.000Z",
      | "payrolledBenefits": 20000.01
      |}
      |""".stripMargin
  )

  val responseModel: RetrieveStudentLoanBIKResponse = RetrieveStudentLoanBIKResponse(
    submittedOn = Timestamp("2025-08-24T14:15:22Z"),
    payrolledBenefits = 20000.01
  )

}
