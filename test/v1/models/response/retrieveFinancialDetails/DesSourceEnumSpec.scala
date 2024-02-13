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

package v1.models.response.retrieveFinancialDetails

import api.models.domain.MtdSourceEnum
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class DesSourceEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[DesSourceEnum](
    ("HMRC HELD", DesSourceEnum.`HMRC HELD`),
    ("CUSTOMER", DesSourceEnum.CUSTOMER),
    ("LATEST", DesSourceEnum.LATEST)
  )

  "toMtdEnum" must {
    "return the expected 'MtdSourceEnum' object" in {
      DesSourceEnum.`HMRC HELD`.toMtdEnum shouldBe MtdSourceEnum.hmrcHeld
      DesSourceEnum.LATEST.toMtdEnum shouldBe MtdSourceEnum.latest
      DesSourceEnum.CUSTOMER.toMtdEnum shouldBe MtdSourceEnum.user
    }
  }

}
