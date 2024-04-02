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

package api.models.downstream

import api.models.domain.SharesAwardedOrReceivedSchemeType
import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait DownstreamSharesAwardedOrReceivedSchemeType {
  def toMtd: SharesAwardedOrReceivedSchemeType
}

object DownstreamSharesAwardedOrReceivedSchemeType {

  case object `SIP` extends DownstreamSharesAwardedOrReceivedSchemeType {
    override def toMtd: SharesAwardedOrReceivedSchemeType = SharesAwardedOrReceivedSchemeType.`sip`
  }

  case object `Other` extends DownstreamSharesAwardedOrReceivedSchemeType {
    override def toMtd: SharesAwardedOrReceivedSchemeType = SharesAwardedOrReceivedSchemeType.`other`
  }

  implicit val reads: Reads[DownstreamSharesAwardedOrReceivedSchemeType] = Enums.reads[DownstreamSharesAwardedOrReceivedSchemeType]
}
