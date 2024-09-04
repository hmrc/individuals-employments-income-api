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

package v1.models.response.retrieveFinancialDetails

import api.models.domain.MtdSourceEnum
import api.models.downstream.DownstreamSourceEnum
import shared.models.domain.Timestamp
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveEmploymentAndFinancialDetailsResponse(submittedOn: Timestamp,
                                                         source: Option[MtdSourceEnum],
                                                         customerAdded: Option[Timestamp],
                                                         dateIgnored: Option[Timestamp],
                                                         employment: Employment)

object RetrieveEmploymentAndFinancialDetailsResponse {
  implicit val writes: OWrites[RetrieveEmploymentAndFinancialDetailsResponse] = Json.writes[RetrieveEmploymentAndFinancialDetailsResponse]

  implicit val reads: Reads[RetrieveEmploymentAndFinancialDetailsResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "source").readNullable[DownstreamSourceEnum].map(_.map(_.toMtdEnum)) and
      (JsPath \ "customerAdded").readNullable[Timestamp] and
      (JsPath \ "dateIgnored").readNullable[Timestamp] and
      (JsPath \ "employment").read[Employment]
  )(RetrieveEmploymentAndFinancialDetailsResponse.apply _)

}
