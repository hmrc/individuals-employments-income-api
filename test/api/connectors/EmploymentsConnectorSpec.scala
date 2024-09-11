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

import mocks.{MockEmploymentsAppConfig, MockFeatureSwitches}
import org.scalamock.handlers.CallHandler
import shared.connectors.ConnectorSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait EmploymentsConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  val otherHeaders: Seq[(String, String)] = List(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
  )

  val dummyIfsHeaderCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      Seq("^not-test-BaseUrl?$".r),
      Seq.empty[String],
      Some("individuals-employments-income-api")
    )

  val requiredRelease6Headers: Seq[(String, String)] = List(
    "Environment"   -> "release6-environment",
    "Authorization" -> s"Bearer release6-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredApi1661Headers: Seq[(String, String)] = List(
    "Environment"   -> "api1661-environment",
    "Authorization" -> s"Bearer api1661-token",
    "CorrelationId" -> s"$correlationId"
  )

  protected trait EmploymentsConnectorTest extends StandardConnectorTest with MockEmploymentsAppConfig {

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]
    protected def excludedHeaders: Seq[(String, String)] = Seq("AnotherHeader" -> "HeaderValue")

    protected def willPut[T](url: String): CallHandler[Future[T]] =
      MockedHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = "",
          excludedHeaders = excludedHeaders
        )
  }

  protected trait Release6Test extends EmploymentsConnectorTest {
    val name = "release6"

    MockedEmploymentsAppConfig.release6DownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait Api1661Test extends EmploymentsConnectorTest {
    val name = "api1661"

    MockedEmploymentsAppConfig.api1661DownstreamConfig.anyNumberOfTimes() returns config
  }

}
