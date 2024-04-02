/*
 * Copyright 2024 HM Revenue & Customs
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

package api.models.downstream

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import DownstreamPayFrequency._
import api.models.domain.PayFrequency

class DownstreamPayFrequencySpec extends UnitSpec with EnumJsonSpecSupport {

  testDeserialization[DownstreamPayFrequency](
    "WEEKLY"           -> `WEEKLY`,
    "FORTNIGHTLY"      -> `FORTNIGHTLY`,
    "FOUR WEEKLY"      -> `FOUR WEEKLY`,
    "CALENDAR MONTHLY" -> `CALENDAR MONTHLY`,
    "QUARTERLY"        -> `QUARTERLY`,
    "BI-ANNUALLY"      -> `BI-ANNUALLY`,
    "ONE-OFF"          -> `ONE-OFF`,
    "IRREGULAR"        -> `IRREGULAR`,
    "ANNUALLY"         -> `ANNUALLY`
  )

  "DownstreamPayFrequency" must {
    "convert to MTD values correctly" in {
      `WEEKLY`.toMtd shouldBe PayFrequency.`weekly`
      `FORTNIGHTLY`.toMtd shouldBe PayFrequency.`fortnightly`
      `FOUR WEEKLY`.toMtd shouldBe PayFrequency.`four-weekly`
      `CALENDAR MONTHLY`.toMtd shouldBe PayFrequency.`monthly`
      `QUARTERLY`.toMtd shouldBe PayFrequency.`quarterly`
      `BI-ANNUALLY`.toMtd shouldBe PayFrequency.`bi-annually`
      `ONE-OFF`.toMtd shouldBe PayFrequency.`one-off`
      `IRREGULAR`.toMtd shouldBe PayFrequency.`irregular`
      `ANNUALLY`.toMtd shouldBe PayFrequency.`annually`
    }
  }

}
