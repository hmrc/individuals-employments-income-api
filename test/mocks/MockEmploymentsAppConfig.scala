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
import shared.config.{DownstreamConfig, MockAppConfig}
import shared.models.domain.TaxYear
import uk.gov.hmrc.http.HeaderCarrier

trait MockEmploymentsAppConfig extends MockFactory {
  val mockEmploymentsConfig: EmploymentsAppConfig = mock[EmploymentsAppConfig]

  object MockEmploymentsAppConfig extends MockAppConfig {
    val minimumPermittedTaxYear: TaxYear = mockEmploymentsConfig.minimumPermittedTaxYear
    lazy val release6DownstreamConfig: DownstreamConfig = mockEmploymentsConfig.release6DownstreamConfig
    lazy val api1661DownstreamConfig: DownstreamConfig = mockEmploymentsConfig.api1661DownstreamConfig

    implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

    val otherHeaders: Seq[(String, String)] = List(
      "Gov-Test-Scenario" -> "DEFAULT",
      "AnotherHeader" -> "HeaderValue"
    )

    val dummyDesHeaderCarrierConfig: HeaderCarrier.Config =
      HeaderCarrier.Config(
        Seq("^not-test-BaseUrl?$".r),
        Seq.empty[String],
        Some("individuals-employments-income-api")
      )

    val dummyIfsHeaderCarrierConfig: HeaderCarrier.Config =
      HeaderCarrier.Config(
        Seq("^not-test-BaseUrl?$".r),
        Seq.empty[String],
        Some("individuals-employments-income-api")
      )

    val dummyHeaderCarrierConfig: HeaderCarrier.Config =
      HeaderCarrier.Config(
        Seq("^not-test-BaseUrl?$".r),
        Seq.empty[String],
        Some("individuals-employments-income-api")
      )

    val allowedDesHeaders: Seq[String] = List(
      "Accept",
      "Gov-Test-Scenario",
      "Content-Type",
      "Location",
      "X-Request-Timestamp",
      "X-Session-Id"
    )

    val allowedIfsHeaders: Seq[String] = List(
      "Accept",
      "Gov-Test-Scenario",
      "Content-Type",
      "Location",
      "X-Request-Timestamp",
      "X-Session-Id"
    )

    val requiredDesHeaders: Seq[(String, String)] = List(
      "Environment" -> "des-environment",
      "Authorization" -> s"Bearer des-token",
      "CorrelationId" -> s"$correlationId"
    )

    val requiredIfsHeaders: Seq[(String, String)] = List(
      "Environment" -> "ifs-environment",
      "Authorization" -> s"Bearer ifs-token",
      "CorrelationId" -> s"$correlationId"
    )

    val requiredTysIfsHeaders: Seq[(String, String)] = List(
      "Environment" -> "TYS-IFS-environment",
      "Authorization" -> s"Bearer TYS-IFS-token",
      "CorrelationId" -> s"$correlationId"
    )

    val requiredRelease6Headers: Seq[(String, String)] = List(
      "Environment" -> "release6-environment",
      "Authorization" -> s"Bearer release6-token",
      "CorrelationId" -> s"$correlationId"
    )

    val requiredApi1661Headers: Seq[(String, String)] = List(
      "Environment" -> "api1661-environment",
      "Authorization" -> s"Bearer api1661-token",
      "CorrelationId" -> s"$correlationId"
    )
  }
}
