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

package api.connectors

import config.{MockEmploymentsAppConfig, MockFeatureSwitches}
import shared.connectors.ConnectorSpec

trait EmploymentsConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  protected trait EmploymentsConnectorTest extends StandardConnectorTest with MockEmploymentsAppConfig

  protected trait Release6Test extends EmploymentsConnectorTest {
    val name = "release6"

    MockedEmploymentsAppConfig.release6DownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait Api1661Test extends EmploymentsConnectorTest {
    val name = "api1661"

    MockedEmploymentsAppConfig.api1661DownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait EmploymentsTysIfsTest extends EmploymentsConnectorTest {
    override val name = "tys-ifs"

    MockedEmploymentsAppConfig.tysIfsDownstreamConfig.anyNumberOfTimes() returns config
  }
}
