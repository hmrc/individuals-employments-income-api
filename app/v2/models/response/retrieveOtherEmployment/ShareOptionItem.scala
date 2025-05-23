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

import common.models.domain.ShareOptionSchemeType
import common.models.downstream.DownstreamShareOptionSchemeType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class ShareOptionItem(employerName: String,
                           employerRef: Option[String],
                           schemePlanType: ShareOptionSchemeType,
                           dateOfOptionGrant: String,
                           dateOfEvent: String,
                           optionNotExercisedButConsiderationReceived: Option[Boolean],
                           amountOfConsiderationReceived: BigDecimal,
                           noOfSharesAcquired: BigInt,
                           classOfSharesAcquired: Option[String],
                           exercisePrice: BigDecimal,
                           amountPaidForOption: BigDecimal,
                           marketValueOfSharesOnExcise: BigDecimal,
                           profitOnOptionExercised: BigDecimal,
                           employersNicPaid: BigDecimal,
                           taxableAmount: BigDecimal)

object ShareOptionItem {

  implicit val reads: Reads[ShareOptionItem] = (
    (JsPath \ "employerName").read[String] and
      (JsPath \ "employerRef").readNullable[String] and
      (JsPath \ "schemePlanType").read[DownstreamShareOptionSchemeType].map(_.toMtd) and
      (JsPath \ "dateOfOptionGrant").read[String] and
      (JsPath \ "dateOfEvent").read[String] and
      (JsPath \ "optionNotExercisedButConsiderationReceived").readNullable[Boolean] and
      (JsPath \ "amountOfConsiderationReceived").read[BigDecimal] and
      (JsPath \ "noOfSharesAcquired").read[BigInt] and
      (JsPath \ "classOfSharesAcquired").readNullable[String] and
      (JsPath \ "exercisePrice").read[BigDecimal] and
      (JsPath \ "amountPaidForOption").read[BigDecimal] and
      (JsPath \ "marketValueOfSharesOnExcise").read[BigDecimal] and
      (JsPath \ "profitOnOptionExercised").read[BigDecimal] and
      (JsPath \ "employersNicPaid").read[BigDecimal] and
      (JsPath \ "taxableAmount").read[BigDecimal]
  )(ShareOptionItem.apply _)

  implicit val writes: OWrites[ShareOptionItem] = Json.writes[ShareOptionItem]
}
