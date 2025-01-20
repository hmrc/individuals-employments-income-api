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

package v2.controllers.validators.resolvers

import shared.controllers.validators.resolvers.{ResolveStringPattern, ResolverSupport}
import cats.data.Validated
import common.errors.EmploymentIdFormatError
import common.models.domain.EmploymentId
import shared.models.errors.MtdError

object ResolveEmploymentId extends ResolverSupport {

  private val employmentIdRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".r

  val resolver: Resolver[String, EmploymentId] =
    ResolveStringPattern(employmentIdRegex, EmploymentIdFormatError).resolver.map(EmploymentId)

  def apply(value: String): Validated[Seq[MtdError], EmploymentId] = resolver(value)

}
