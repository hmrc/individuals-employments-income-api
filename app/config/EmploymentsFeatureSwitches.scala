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

package config

import org.apache.commons.lang3.BooleanUtils
import play.api.Configuration
import play.api.mvc.Request
import shared.config.{AppConfig, FeatureSwitches}

import javax.inject.Inject

case class EmploymentsFeatureSwitches (protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: AppConfig) = this(appConfig.featureSwitchConfig)

  def isDesIf_MigrationEnabled: Boolean = isEnabled("desIf_Migration")

  def isTemporalValidationEnabled(implicit request: Request[_]): Boolean = {
    if (isEnabled("allowTemporalValidationSuspension")) {
      request.headers.get("suspend-temporal-validations").forall(!BooleanUtils.toBoolean(_))
    } else {
      true
    }
  }

}

object EmploymentsFeatureSwitches {
  def apply(configuration: Configuration): EmploymentsFeatureSwitches = new EmploymentsFeatureSwitches(configuration)

  def apply()(implicit appConfig: AppConfig): EmploymentsFeatureSwitches = new EmploymentsFeatureSwitches(appConfig)
}