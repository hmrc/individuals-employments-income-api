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

package common.models.domain

import play.api.libs.json.Writes
import shared.utils.enums.Enums

enum ShareOptionSchemeType(toDownstreamString: String) {
  case `emi`   extends ShareOptionSchemeType("EMI")
  case `csop`  extends ShareOptionSchemeType("CSOP")
  case `saye`  extends ShareOptionSchemeType("SAYE")
  case `other` extends ShareOptionSchemeType("Other")
}

object ShareOptionSchemeType {
  given Writes[ShareOptionSchemeType]         = Enums.writes[ShareOptionSchemeType]
  val parser: PartialFunction[String, ShareOptionSchemeType] = Enums.parser[ShareOptionSchemeType](values)
}
