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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AmendRedundancyCompensationPaymentsOverExemptionItem(amount: BigDecimal, taxPaid: Option[BigDecimal], taxTakenOffInEmployment: Option[Boolean])

object AmendRedundancyCompensationPaymentsOverExemptionItem {
  implicit val reads: Reads[AmendRedundancyCompensationPaymentsOverExemptionItem] = Json.reads[AmendRedundancyCompensationPaymentsOverExemptionItem]

  implicit val writes: OWrites[AmendRedundancyCompensationPaymentsOverExemptionItem] = (
    (JsPath \ "amount").write[BigDecimal] and
      (JsPath \ "taxPaid").writeNullable[BigDecimal] and
      (JsPath \ "taxTakenOffInEmployment").writeNullable[Boolean]
  )(unlift(AmendRedundancyCompensationPaymentsOverExemptionItem.unapply))
}
