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

package mocks

import config.EmploymentsAppConfig
import org.scalamock.handlers.CallHandler0
import org.scalamock.scalatest.MockFactory
import shared.config.{DownstreamConfig, MockAppConfig}
import shared.models.domain.TaxYear

trait MockEmploymentsAppConfig extends MockFactory with MockAppConfig {
  val mockEmploymentsConfig: EmploymentsAppConfig = mock[EmploymentsAppConfig]

  object MockedEmploymentsAppConfig {
    def minimumPermittedTaxYear: CallHandler0[TaxYear] = (() => mockEmploymentsConfig.minimumPermittedTaxYear: TaxYear).expects()
    def release6DownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.release6DownstreamConfig: DownstreamConfig).expects()
    def api1661DownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.api1661DownstreamConfig: DownstreamConfig).expects()
  }

}
