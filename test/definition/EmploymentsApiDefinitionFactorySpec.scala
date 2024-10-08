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

package definition

import cats.implicits.catsSyntaxValidatedId
import config.MockEmploymentsAppConfig
import shared.config.Deprecation.NotDeprecated
import shared.config.{ConfidenceLevelConfig, MockSharedAppConfig}
import shared.definition.APIStatus.BETA
import shared.definition.{APIDefinition, APIVersion, Definition}
import shared.mocks.MockHttpClient
import shared.routing.{Version1, Version2}
import shared.utils.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class EmploymentsApiDefinitionFactorySpec extends UnitSpec with MockEmploymentsAppConfig with MockSharedAppConfig {

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  class Test extends MockHttpClient with MockSharedAppConfig {
    val apiDefinitionFactory = new EmploymentsApiDefinitionFactory(mockSharedAppConfig)
    MockedSharedAppConfig.apiGatewayContext returns "individuals/employments-income"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        List(Version1, Version2).foreach { version =>
          MockedSharedAppConfig.apiStatus(version) returns "BETA"
          MockedSharedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()
          MockedSharedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }
        MockedSharedAppConfig.confidenceLevelConfig
          .returns(ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = true, authValidationEnabled = true))
          .anyNumberOfTimes()

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Individuals Employments Income (MTD)",
              description = "An API for providing individual employments income data",
              context = "individuals/employments-income",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version1,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version2,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

}
