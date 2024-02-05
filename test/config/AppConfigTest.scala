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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration

/** Temporary test to get code coverage up; will be replaced with the real API code & tests.
 */
class AppConfigTest extends AnyWordSpec with Matchers {

  "appName" should {
    "return the configured app name" in {
      val config    = Configuration.from(Map("appName" -> "Employments Income API"))
      val appConfig = new AppConfig(config)
      val result    = appConfig.appName
      result shouldBe "Employments Income API"
    }
  }

}