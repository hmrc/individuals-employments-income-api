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

package v1.controllers.validators.resolvers

import api.models.domain.EmploymentId
import cats.data.Validated.{Invalid, Valid}
import common.errors.EmploymentIdFormatError
import shared.utils.UnitSpec

class ResolveEmploymentIdSpec extends UnitSpec {

  "ResolveEmploymentId" should {
    "return no errors" when {
      "given a valid ID" in {
        val id = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
        ResolveEmploymentId(id) shouldBe Valid(EmploymentId(id))
      }
    }

    "return an error" when {
      "given an invalid ID" in {
        ResolveEmploymentId("BAD_ID") shouldBe Invalid(List(EmploymentIdFormatError))
      }
    }
  }

}
