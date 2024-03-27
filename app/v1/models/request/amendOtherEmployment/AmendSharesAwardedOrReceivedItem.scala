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

import api.models.domain.SharesAwardedOrReceivedSchemeType
import play.api.libs.json.{Json, OWrites, Reads}

case class AmendSharesAwardedOrReceivedItem(employerName: String,
                                            employerRef: Option[String],
                                            schemePlanType: String,
                                            dateSharesCeasedToBeSubjectToPlan: String,
                                            noOfShareSecuritiesAwarded: BigInt,
                                            classOfShareAwarded: String,
                                            dateSharesAwarded: String,
                                            sharesSubjectToRestrictions: Boolean,
                                            electionEnteredIgnoreRestrictions: Boolean,
                                            actualMarketValueOfSharesOnAward: BigDecimal,
                                            unrestrictedMarketValueOfSharesOnAward: BigDecimal,
                                            amountPaidForSharesOnAward: BigDecimal,
                                            marketValueAfterRestrictionsLifted: BigDecimal,
                                            taxableAmount: BigDecimal)

object AmendSharesAwardedOrReceivedItem {

  implicit val reads: Reads[AmendSharesAwardedOrReceivedItem] = Json.reads[AmendSharesAwardedOrReceivedItem]

  implicit val writes: OWrites[AmendSharesAwardedOrReceivedItem] = (requestBody: AmendSharesAwardedOrReceivedItem) => {

    val schemeType = SharesAwardedOrReceivedSchemeType.parser(requestBody.schemePlanType)
    Json.obj(
      "employerName" -> requestBody.employerName,
      "employerRef" -> requestBody.employerRef,
      "schemePlanType" -> schemeType.toDesViewString,
      "dateSharesCeasedToBeSubjectToPlan" -> requestBody.dateSharesCeasedToBeSubjectToPlan,
      "noOfShareSecuritiesAwarded" -> requestBody.noOfShareSecuritiesAwarded,
      "classOfShareAwarded" -> requestBody.classOfShareAwarded,
      "dateSharesAwarded" -> requestBody.dateSharesAwarded,
      "sharesSubjectToRestrictions" -> requestBody.sharesSubjectToRestrictions,
      "electionEnteredIgnoreRestrictions" -> requestBody.electionEnteredIgnoreRestrictions,
      "actualMarketValueOfSharesOnAward" -> requestBody.actualMarketValueOfSharesOnAward,
      "unrestrictedMarketValueOfSharesOnAward" -> requestBody.unrestrictedMarketValueOfSharesOnAward,
      "amountPaidForSharesOnAward" -> requestBody.amountPaidForSharesOnAward,
      "marketValueAfterRestrictionsLifted" -> requestBody.marketValueAfterRestrictionsLifted,
      "taxableAmount" -> requestBody.taxableAmount
    )
  }
}
