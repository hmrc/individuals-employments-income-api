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

package v1.controllers.validators

import api.controllers.validators.{MockValidatorFactory, Validator}
import org.scalamock.handlers.CallHandler
import v1.models.request.listEmployments.ListEmploymentsRequest

trait MockListEmploymentsValidatorFactory extends MockValidatorFactory[ListEmploymentsRequest] {

  val mockListEmploymentsValidatorFactory: ListEmploymentsValidatorFactory = mock[ListEmploymentsValidatorFactory]

  def validator(): CallHandler[Validator[ListEmploymentsRequest]] =
    (mockListEmploymentsValidatorFactory
      .validator(_: String, _: String))
      .expects(*, *)

}