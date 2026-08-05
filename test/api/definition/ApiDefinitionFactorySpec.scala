/*
 * Copyright 2026 HM Revenue & Customs
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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.MockAppConfig
import api.definition.APIAccessType.PUBLIC
import api.definition.APIStatus.{ALPHA, BETA}
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId
import play.api.libs.json.Json

import scala.language.reflectiveCalls

class ApiDefinitionFactorySpec extends UnitSpec {

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      "return the expected status" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "BETA"

        val result: APIStatus = checkBuildApiStatus(Version9)
        result shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      "default to alpha" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "not-a-status"

        checkBuildApiStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "DEPRECATED"

        MockedAppConfig
          .deprecationFor(Version9)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          checkBuildApiStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }

    "set the access level" when {
      "the controlled access flag is enabled" should {
        "to be CONTROLLED" in new Test {
          MockedAppConfig.endpointsEnabled(Version2) returns true
          setupMockConfig(Version2)
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.controlledAccessEnabled returns true

          apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
        }
      }

      "the controlled access flag is disabled" should {
        "return PUBLIC" in new Test {
          MockedAppConfig.endpointsEnabled(Version2) returns true
          setupMockConfig(Version2)
          MockedAppConfig.apiStatus(Version2) returns "BETA"
          MockedAppConfig.controlledAccessEnabled returns false

          apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
        }
      }
    }
  }

  "APIVersion Json.format" should {

    "round-trip successfully" in {
      val model = APIVersion(
        version = Version2,
        status = APIStatus.BETA,
        access = PUBLIC,
        endpointsEnabled = true
      )

      val json = Json.toJson(model)

      json.as[APIVersion] shouldBe model
    }
  }

  trait Test extends UnitSpec with MockHttpClient with MockAppConfig {
    MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"
    val apiDefinitionFactory: ApiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)

    def checkBuildApiStatus(version: Version): APIStatus = apiDefinitionFactory.buildAPIStatus(version)

    protected def setupMockConfig(version: Version): Unit = {
      MockedAppConfig
        .deprecationFor(version)
        .returns(NotDeprecated.valid)
        .anyNumberOfTimes()
    }

  }

}
