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

package common.models.downstream

import common.models.domain.PayFrequency
import play.api.libs.json.Reads
import shared.utils.enums.Enums

sealed trait DownstreamPayFrequency {
  def toMtd: PayFrequency
}

object DownstreamPayFrequency {

  case object `WEEKLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`weekly`
  }

  case object `FORTNIGHTLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`fortnightly`
  }

  case object `FOUR WEEKLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`four-weekly`
  }

  case object `CALENDAR MONTHLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`monthly`
  }

  case object `QUARTERLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`quarterly`
  }

  case object `BI-ANNUALLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`bi-annually`
  }

  case object `ONE-OFF` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`one-off`
  }

  case object `IRREGULAR` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`irregular`
  }

  case object `ANNUALLY` extends DownstreamPayFrequency {
    override def toMtd: PayFrequency = PayFrequency.`annually`
  }

  implicit val reads: Reads[DownstreamPayFrequency] = Enums.reads[DownstreamPayFrequency]

}
