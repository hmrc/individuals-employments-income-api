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

import com.google.inject.ImplementedBy
import org.apache.commons.lang3.BooleanUtils
import play.api.Configuration
import play.api.mvc.Request

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[FeatureSwitchesImpl])
trait FeatureSwitches {

  def isEnabled(feature: String): Boolean

  def isTemporalValidationEnabled(implicit request: Request[_]): Boolean

  def isDesIf_MigrationEnabled: Boolean

  def supportingAgentsAccessControlEnabled: Boolean
}

@Singleton
class FeatureSwitchesImpl(featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: AppConfig) = this(appConfig.featureSwitches)

  val isDesIf_MigrationEnabled: Boolean = isEnabled("desIf_Migration")
  val supportingAgentsAccessControlEnabled: Boolean = isEnabled("supporting-agents-access-control")

  def isTemporalValidationEnabled(implicit request: Request[_]): Boolean = {
    if (isEnabled("allowTemporalValidationSuspension")) {
      request.headers.get("suspend-temporal-validations").forall(!BooleanUtils.toBoolean(_))
    } else {
      true
    }
  }

  def isEnabled(feature: String): Boolean = isConfigTrue(feature + ".enabled")

  private def isConfigTrue(key: String): Boolean = featureSwitchConfig.getOptional[Boolean](key).getOrElse(true)
}

object FeatureSwitches {
  def apply(configuration: Configuration): FeatureSwitches = new FeatureSwitchesImpl(configuration)

  def apply(appConfig: AppConfig): FeatureSwitches = new FeatureSwitchesImpl(appConfig)
}
