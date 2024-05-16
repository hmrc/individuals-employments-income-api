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

import api.models.domain.Timestamp
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils

case class RetrieveOtherEmploymentResponse(submittedOn: Timestamp,
                                           shareOption: Option[Seq[ShareOptionItem]],
                                           sharesAwardedOrReceived: Option[Seq[SharesAwardedOrReceivedItem]],
                                           disability: Option[CommonOtherEmployment],
                                           foreignService: Option[CommonOtherEmployment],
                                           lumpSums: Option[Seq[LumpSums]])

object RetrieveOtherEmploymentResponse extends JsonUtils {

  implicit val reads: Reads[RetrieveOtherEmploymentResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "shareOption").readNullable[Seq[ShareOptionItem]].mapEmptySeqToNone and
      (JsPath \ "sharesAwardedOrReceived").readNullable[Seq[SharesAwardedOrReceivedItem]].mapEmptySeqToNone and
      (JsPath \ "disability").readNullable[CommonOtherEmployment] and
      (JsPath \ "foreignService").readNullable[CommonOtherEmployment] and
      (JsPath \ "lumpSums").readNullable[Seq[LumpSums]].mapEmptySeqToNone
  )(RetrieveOtherEmploymentResponse.apply _)

  implicit val writes: OWrites[RetrieveOtherEmploymentResponse] = Json.writes[RetrieveOtherEmploymentResponse]

}
