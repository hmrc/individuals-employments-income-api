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

package v2.models.response.retrieveStudentLoanBIK

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.models.domain.Timestamp

case class RetrieveStudentLoanBIKResponse (submittedOn: Timestamp,
                                      payrolledBenefits: BigDecimal) {

}

object RetrieveStudentLoanBIKResponse {


  implicit val reads: Reads[RetrieveStudentLoanBIKResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "payrolledBenefits").read[BigDecimal]
    )(RetrieveStudentLoanBIKResponse.apply _)

  implicit val writes: OWrites[RetrieveStudentLoanBIKResponse] = Json.writes[RetrieveStudentLoanBIKResponse]


}