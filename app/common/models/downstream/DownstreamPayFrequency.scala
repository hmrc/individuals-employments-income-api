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

enum DownstreamPayFrequency(toMtd: PayFrequency) {
  case `WEEKLY`           extends DownstreamPayFrequency(PayFrequency.`weekly`)
  case `FORTNIGHTLY`      extends DownstreamPayFrequency(PayFrequency.`fortnightly`)
  case `FOUR WEEKLY`      extends DownstreamPayFrequency(PayFrequency.`four-weekly`)
  case `CALENDAR MONTHLY` extends DownstreamPayFrequency(PayFrequency.`monthly`)
  case `QUARTERLY`        extends DownstreamPayFrequency(PayFrequency.`quarterly`)
  case `BI-ANNUALLY`      extends DownstreamPayFrequency(PayFrequency.`bi-annually`)
  case `ONE-OFF`          extends DownstreamPayFrequency(PayFrequency.`one-off`)
  case `IRREGULAR`        extends DownstreamPayFrequency(PayFrequency.`irregular`)
  case `ANNUALLY`         extends DownstreamPayFrequency(PayFrequency.`annually`)
}

object DownstreamPayFrequency {
  given Reads[DownstreamPayFrequency] = Enums.reads[DownstreamPayFrequency](values)
}
