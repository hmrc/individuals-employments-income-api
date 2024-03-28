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

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait ShareOptionSchemeType{
  def toDesViewString: String
}

object ShareOptionSchemeType {
  case object `emi`   extends ShareOptionSchemeType{
    override def toDesViewString: String = "EMI"
  }
  case object `csop`  extends ShareOptionSchemeType{
    override def toDesViewString: String = "CSOP"
  }
  case object `saye`  extends ShareOptionSchemeType{
    override def toDesViewString: String = "SAYE"
  }
  case object `other` extends ShareOptionSchemeType{
    override def toDesViewString: String = "Other"
  }

  implicit val format: Format[ShareOptionSchemeType] = Enums.format[ShareOptionSchemeType]

  val parser: PartialFunction[String, ShareOptionSchemeType] = Enums.parser[ShareOptionSchemeType]

}
