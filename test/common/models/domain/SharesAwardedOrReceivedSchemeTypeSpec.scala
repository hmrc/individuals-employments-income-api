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

package common.models.domain

import common.models.domain.SharesAwardedOrReceivedSchemeType._
import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport

class SharesAwardedOrReceivedSchemeTypeSpec extends UnitSpec with EnumJsonSpecSupport {

  testSerialization[SharesAwardedOrReceivedSchemeType](
    `sip`   -> "sip",
    `other` -> "other"
  )

  "SharesAwardedOrReceivedSchemeType" must {
    "convert to downstream string correctly" in {
      `sip`.toDownstreamString shouldBe "SIP"
      `other`.toDownstreamString shouldBe "Other"
    }
  }

}
