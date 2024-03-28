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
import play.api.libs.json.{Json, OWrites, Reads}

case class AmendShareOptionItem(employerName: String,
                                employerRef: Option[String],
                                schemePlanType: String,
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

  implicit val reads: Reads[AmendShareOptionItem] = Json.reads[AmendShareOptionItem]

  implicit val writes: OWrites[AmendShareOptionItem] = (requestBody: AmendShareOptionItem) => {

    val schemeType = ShareOptionSchemeType.parser(requestBody.schemePlanType)
    Json.obj(
      "employerName" -> requestBody.employerName,
      "employerRef" -> requestBody.employerRef,
      "schemePlanType" -> schemeType.toDesViewString,
      "dateOfOptionGrant" -> requestBody.dateOfOptionGrant,
      "dateOfEvent" -> requestBody.dateOfEvent,
      "optionNotExercisedButConsiderationReceived" -> requestBody.optionNotExercisedButConsiderationReceived,
      "amountOfConsiderationReceived" -> requestBody.amountOfConsiderationReceived,
      "noOfSharesAcquired" -> requestBody.noOfSharesAcquired,
      "classOfSharesAcquired" -> requestBody.classOfSharesAcquired,
      "exercisePrice" -> requestBody.exercisePrice,
      "amountPaidForOption" -> requestBody.amountPaidForOption,
      "marketValueOfSharesOnExcise" -> requestBody.marketValueOfSharesOnExcise,
      "profitOnOptionExercised" -> requestBody.profitOnOptionExercised,
      "employersNicPaid" -> requestBody.employersNicPaid,
      "taxableAmount" -> requestBody.taxableAmount
    )
  }
}
