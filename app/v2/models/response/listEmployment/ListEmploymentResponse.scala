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

package v2.models.response.listEmployment

import common.utils.JsonUtils
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ListEmploymentResponse(employments: Option[Seq[Employment]], customEmployments: Option[Seq[Employment]])

object ListEmploymentResponse extends JsonUtils {

  implicit val writes: OWrites[ListEmploymentResponse] = Json.writes

  implicit val reads: Reads[ListEmploymentResponse] = (
    (JsPath \ "employments").readNullable[Seq[Employment]].mapEmptySeqToNone and
      (JsPath \ "customerDeclaredEmployments").readNullable[Seq[Employment]].mapEmptySeqToNone
  )((employments, customerEmployments) => ListEmploymentResponse(employments, customerEmployments))

}
