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

import cats.data.Validated
import config.EmploymentsAppConfig
import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import shared.config._
import shared.models.domain.TaxYear
import shared.routing.Version

trait MockEmploymentsAppConfig extends MockFactory with MockAppConfig {
  implicit val mockEmploymentsConfig: EmploymentsAppConfig = mock[EmploymentsAppConfig]

  object MockedEmploymentsAppConfig {
    def minimumPermittedTaxYear: CallHandler0[TaxYear] = (() => mockEmploymentsConfig.minimumPermittedTaxYear: TaxYear).expects()
    def release6DownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.release6DownstreamConfig: DownstreamConfig).expects()
    def api1661DownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.api1661DownstreamConfig: DownstreamConfig).expects()

    // MTD ID Lookup Config
    def mtdIdBaseUrl: CallHandler0[String] = (() => mockEmploymentsConfig.mtdIdBaseUrl: String).expects()

    def desDownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.desDownstreamConfig: DownstreamConfig).expects()
    def ifsDownstreamConfig: CallHandler0[DownstreamConfig]    = (() => mockEmploymentsConfig.ifsDownstreamConfig: DownstreamConfig).expects()
    def tysIfsDownstreamConfig: CallHandler0[DownstreamConfig] = (() => mockEmploymentsConfig.tysIfsDownstreamConfig: DownstreamConfig).expects()

    def hipDownstreamConfig: CallHandler[BasicAuthDownstreamConfig] = (() => mockEmploymentsConfig.hipDownstreamConfig: BasicAuthDownstreamConfig).expects()

    // API Config
    def featureSwitchConfig: CallHandler0[Configuration]         = (() => mockEmploymentsConfig.featureSwitchConfig: Configuration).expects()
    def apiGatewayContext: CallHandler0[String]                  = (() => mockEmploymentsConfig.apiGatewayContext: String).expects()
    def apiStatus(version: Version): CallHandler[String]         = (mockEmploymentsConfig.apiStatus: Version => String).expects(version)
    def endpointsEnabled(version: String): CallHandler[Boolean]  = (mockEmploymentsConfig.endpointsEnabled(_: String)).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockEmploymentsConfig.endpointsEnabled: Version => Boolean).expects(version)
    def deprecationFor(version: Version): CallHandler[Validated[String, Deprecation]] = (mockEmploymentsConfig.deprecationFor(_: Version)).expects(version)
    def apiDocumentationUrl(): CallHandler[String]                                    = (() => mockEmploymentsConfig.apiDocumentationUrl: String).expects()

    def apiVersionReleasedInProduction(version: String): CallHandler[Boolean] =
      (mockEmploymentsConfig.apiVersionReleasedInProduction: String => Boolean).expects(version)

    def endpointReleasedInProduction(version: String, key: String): CallHandler[Boolean] =
      (mockEmploymentsConfig.endpointReleasedInProduction: (String, String) => Boolean).expects(version, key)

    def confidenceLevelConfig: CallHandler0[ConfidenceLevelConfig] =
      (() => mockEmploymentsConfig.confidenceLevelConfig).expects()

    def endpointAllowsSupportingAgents(endpointName: String): CallHandler[Boolean] =
      (mockEmploymentsConfig.endpointAllowsSupportingAgents(_: String)).expects(endpointName)

    def allowRequestCannotBeFulfilledHeader(version: Version): CallHandler[Boolean] =
      (mockEmploymentsConfig.allowRequestCannotBeFulfilledHeader: Version => Boolean).expects(version)
  }

}
