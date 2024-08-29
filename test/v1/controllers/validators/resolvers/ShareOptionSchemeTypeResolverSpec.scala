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

package v1.controllers.validators.resolvers

import api.models.domain.ShareOptionSchemeType
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ShareOptionSchemeTypeResolverSpec extends UnitSpec {

  private val error = SchemePlanTypeFormatError.withPath("somePath")

  "ShareOptionSchemeTypeResolver" when {
    "the scheme type is valid" must {
      def resolveSuccess(value: String, expected: ShareOptionSchemeType): Unit =
        s"correctly resolve $value" in {
          ShareOptionSchemeTypeResolver.resolver(error)(value) shouldBe Valid(expected)
        }

      behave like resolveSuccess("emi", ShareOptionSchemeType.`emi`)
      behave like resolveSuccess("csop", ShareOptionSchemeType.`csop`)
      behave like resolveSuccess("saye", ShareOptionSchemeType.`saye`)
      behave like resolveSuccess("other", ShareOptionSchemeType.`other`)
    }

    "the scheme type is not valid" must {
      "return the error" in {
        ShareOptionSchemeTypeResolver.resolver(error)("BAD") shouldBe Invalid(Seq(error))
      }
    }

  }

}
