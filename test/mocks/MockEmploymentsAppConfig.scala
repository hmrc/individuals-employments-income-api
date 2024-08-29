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
import org.scalamock.scalatest.MockFactory
import shared.config.MockAppConfig
import shared.models.domain.TaxYear

trait MockEmploymentsAppConfig extends MockFactory {
  val mockCalculationsConfig: EmploymentsAppConfig = mock[EmploymentsAppConfig]

  object MockEmploymentsAppConfig extends MockAppConfig {
    val minimumPermittedTaxYear: TaxYear = mockCalculationsConfig.minimumPermittedTaxYear: TaxYear
  }

}
