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

package api.models.domain

import shared.utils.UnitSpec
import utils.enums.EnumJsonSpecSupport

class MtdSourceEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[MtdSourceEnum](
    ("hmrc-held", MtdSourceEnum.`hmrc-held`),
    ("user", MtdSourceEnum.user),
    ("latest", MtdSourceEnum.latest)
  )

  "toDesViewString" must {
    "return the expected string" in {
      MtdSourceEnum.`hmrc-held`.toDesViewString shouldBe "HMRC-HELD"
      MtdSourceEnum.latest.toDesViewString shouldBe "LATEST"
      MtdSourceEnum.user.toDesViewString shouldBe "CUSTOMER"
    }
  }

}
