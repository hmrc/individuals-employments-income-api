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

package v2.models.response.retrieveFinancialDetails

import common.models.domain.PayFrequency
import common.models.downstream.DownstreamPayFrequency
import play.api.libs.json.{Json, OFormat, Reads}

case class Pay(taxablePayToDate: Option[BigDecimal],
               totalTaxToDate: Option[BigDecimal],
               payFrequency: Option[PayFrequency],
               paymentDate: Option[String],
               taxWeekNo: Option[Int],
               taxMonthNo: Option[Int])

object Pay {

  implicit val format: OFormat[Pay] = {
    implicit val payFrequencyReads: Reads[PayFrequency] = implicitly[Reads[DownstreamPayFrequency]].map(_.toMtd)
    Json.format[Pay]
  }

}
