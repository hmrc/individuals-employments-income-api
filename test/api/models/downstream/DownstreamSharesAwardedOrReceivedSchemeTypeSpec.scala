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

import api.models.domain.SharesAwardedOrReceivedSchemeType
import api.models.downstream.DownstreamSharesAwardedOrReceivedSchemeType._
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class DownstreamSharesAwardedOrReceivedSchemeTypeSpec extends UnitSpec with EnumJsonSpecSupport {

  testDeserialization[DownstreamSharesAwardedOrReceivedSchemeType](
    "SIP"   -> `SIP`,
    "Other" -> `Other`
  )

  "DownstreamPayFrequency" must {
    "convert to MTD values correctly" in {
      `SIP`.toMtd shouldBe SharesAwardedOrReceivedSchemeType.sip
      `Other`.toMtd shouldBe SharesAwardedOrReceivedSchemeType.`other`
    }
  }

}
