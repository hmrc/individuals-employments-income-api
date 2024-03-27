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

package v1.models.request.amendOtherEmployment

import api.models.domain.ShareOptionSchemeType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OWrites, Reads}

case class AmendShareOptionItem(employerName: String,
                                employerRef: Option[String],
                                schemePlanType: ShareOptionSchemeType,
                                dateOfOptionGrant: String,
                                dateOfEvent: String,
                                optionNotExercisedButConsiderationReceived: Boolean,
                                amountOfConsiderationReceived: BigDecimal,
                                noOfSharesAcquired: BigInt,
                                classOfSharesAcquired: String,
                                exercisePrice: BigDecimal,
                                amountPaidForOption: BigDecimal,
                                marketValueOfSharesOnExcise: BigDecimal,
                                profitOnOptionExercised: BigDecimal,
                                employersNicPaid: BigDecimal,
                                taxableAmount: BigDecimal)

object AmendShareOptionItem {

  implicit val reads: Reads[AmendShareOptionItem] = (
    (JsPath \ "employerName").read[String] and
      (JsPath \ "employerRef").readNullable[String] and
      (JsPath \ "schemePlanType").read[ShareOptionSchemeType] and
      (JsPath \ "dateOfOptionGrant").read[String] and
      (JsPath \ "dateOfEvent").read[String] and
      (JsPath \ "optionNotExercisedButConsiderationReceived").read[Boolean] and
      (JsPath \ "amountOfConsiderationReceived").read[BigDecimal] and
      (JsPath \ "noOfSharesAcquired").read[BigInt] and
      (JsPath \ "classOfSharesAcquired").read[String] and
      (JsPath \ "exercisePrice").read[BigDecimal] and
      (JsPath \ "amountPaidForOption").read[BigDecimal] and
      (JsPath \ "marketValueOfSharesOnExcise").read[BigDecimal] and
      (JsPath \ "profitOnOptionExercised").read[BigDecimal] and
      (JsPath \ "employersNicPaid").read[BigDecimal] and
      (JsPath \ "taxableAmount").read[BigDecimal]
    ) (AmendShareOptionItem.apply _)

  implicit val writes: OWrites[AmendShareOptionItem] = (
    (JsPath \ "employerName").write[String] and
      (JsPath \ "employerRef").writeNullable[String] and
      (JsPath \ "schemePlanType").write[String].contramap[ShareOptionSchemeType](_.toDesViewString) and
      (JsPath \ "dateOfOptionGrant").write[String] and
      (JsPath \ "dateOfEvent").write[String] and
      (JsPath \ "optionNotExercisedButConsiderationReceived").write[Boolean] and
      (JsPath \ "amountOfConsiderationReceived").write[BigDecimal] and
      (JsPath \ "noOfSharesAcquired").write[BigInt] and
      (JsPath \ "classOfSharesAcquired").write[String] and
      (JsPath \ "exercisePrice").write[BigDecimal] and
      (JsPath \ "amountPaidForOption").write[BigDecimal] and
      (JsPath \ "marketValueOfSharesOnExcise").write[BigDecimal] and
      (JsPath \ "profitOnOptionExercised").write[BigDecimal] and
      (JsPath \ "employersNicPaid").write[BigDecimal] and
      (JsPath \ "taxableAmount").write[BigDecimal]
  )(unlift(AmendShareOptionItem.unapply))

}
