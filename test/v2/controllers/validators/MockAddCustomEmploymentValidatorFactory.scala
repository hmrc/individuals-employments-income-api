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

package v2.controllers.validators

import org.scalamock.handlers.CallHandler
import play.api.libs.json.JsValue
import shared.controllers.validators.{MockValidatorFactory, Validator}
import v2.models.request.addCustomEmployment.AddCustomEmploymentRequest

trait MockAddCustomEmploymentValidatorFactory extends MockValidatorFactory[AddCustomEmploymentRequest] {

  val mockAddCustomEmploymentValidatorFactory: AddCustomEmploymentValidatorFactory = mock[AddCustomEmploymentValidatorFactory]

  def validator(): CallHandler[Validator[AddCustomEmploymentRequest]] =
    (mockAddCustomEmploymentValidatorFactory
      .validator(_: String, _: String, _: JsValue, _: Boolean))
      .expects(*, *, *, *)

}
