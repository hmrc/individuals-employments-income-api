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

package config

import play.api.Configuration
import shared.config.{AppConfig, DownstreamConfig}
import shared.models.domain.TaxYear
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class EmploymentsAppConfig @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig(config, configuration){

  def featureSwitches: EmploymentsFeatureSwitches = EmploymentsFeatureSwitches(featureSwitchConfig)

  val minimumPermittedTaxYear: TaxYear = TaxYear.ending(config.getInt("minimumPermittedTaxYear"))

  lazy val release6DownstreamConfig: DownstreamConfig =
    downstreamConfig("release6")

  lazy val api1661DownstreamConfig: DownstreamConfig =
    downstreamConfig("api1661")
}
