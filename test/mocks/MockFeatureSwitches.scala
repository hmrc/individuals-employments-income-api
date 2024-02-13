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

package mocks

import config.FeatureSwitches
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Request

trait MockFeatureSwitches extends MockFactory {

  implicit val mockFeatureSwitches: FeatureSwitches = mock[FeatureSwitches]

  object MockFeatureSwitches {

    def isPassDeleteIntentEnabled: CallHandler[Boolean] =
      (() => mockFeatureSwitches.isPassDeleteIntentEnabled).expects()

    def isOpwEnabled: CallHandler[Boolean] =
      (() => mockFeatureSwitches.isOpwEnabled).expects()

    def isTemporalValidationEnabled(implicit request: Request[_]): CallHandler[Boolean] =
      (mockFeatureSwitches.isTemporalValidationEnabled(_: Request[_])).expects(request)

  }

}
