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

package v2.models.request.amendOtherEmployment

import common.models.domain.ShareOptionSchemeType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AmendShareOptionItem(employerName: String,
                                employerRef: Option[String],
                                schemePlanType: String,
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

object AmendShareOptionItem {

  implicit val reads: Reads[AmendShareOptionItem] = Json.reads[AmendShareOptionItem]

  private def schemeTypeToDownstream(mtdValue: String): String = ShareOptionSchemeType.parser(mtdValue).toDownstreamString

  implicit val writes: OWrites[AmendShareOptionItem] = (
    (JsPath \ "employerName").write[String] and
      (JsPath \ "employerRef").writeNullable[String] and
      (JsPath \ "schemePlanType").write[String].contramap(schemeTypeToDownstream) and
      (JsPath \ "dateOfOptionGrant").write[String] and
      (JsPath \ "dateOfEvent").write[String] and
      (JsPath \ "optionNotExercisedButConsiderationReceived").writeNullable[Boolean] and
      (JsPath \ "amountOfConsiderationReceived").write[BigDecimal] and
      (JsPath \ "noOfSharesAcquired").write[BigInt] and
      (JsPath \ "classOfSharesAcquired").writeNullable[String] and
      (JsPath \ "exercisePrice").write[BigDecimal] and
      (JsPath \ "amountPaidForOption").write[BigDecimal] and
      (JsPath \ "marketValueOfSharesOnExcise").write[BigDecimal] and
      (JsPath \ "profitOnOptionExercised").write[BigDecimal] and
      (JsPath \ "employersNicPaid").write[BigDecimal] and
      (JsPath \ "taxableAmount").write[BigDecimal]
  )(unlift(AmendShareOptionItem.unapply))

}
