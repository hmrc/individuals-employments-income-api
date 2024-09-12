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

import com.google.common.base.Charsets
import mocks.{MockEmploymentsAppConfig, MockFeatureSwitches}
import org.scalamock.handlers.CallHandler
import shared.config.BasicAuthDownstreamConfig
import shared.connectors.ConnectorSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.util.Base64
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

  protected trait EmploymentsDesTest extends EmploymentsConnectorTest {
    val name = "des"

    MockedEmploymentsAppConfig.desDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait EmploymentsIfsTest extends EmploymentsConnectorTest {
    override val name = "ifs"

    MockedEmploymentsAppConfig.ifsDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait EmploymentsTysIfsTest extends EmploymentsConnectorTest {
    override val name = "tys-ifs"

    MockedEmploymentsAppConfig.tysIfsDownstreamConfig.anyNumberOfTimes() returns config
  }

  protected trait EmploymentsHipTest extends ConnectorTest with MockEmploymentsAppConfig {
    private val clientId     = "clientId"
    private val clientSecret = "clientSecret"

    private val token =
      Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(Charsets.UTF_8))

    private val environment = "hip-environment"

    protected final lazy val requiredHeaders: Seq[(String, String)] = List(
      "Authorization"        -> s"Basic $token",
      "Environment"          -> environment,
      "User-Agent"           -> "this-api",
      "CorrelationId"        -> correlationId,
      "Gov-Test-Scenario"    -> "DEFAULT"
    ) ++ intent.map("intent" -> _)

    MockedEmploymentsAppConfig.hipDownstreamConfig
      .anyNumberOfTimes() returns BasicAuthDownstreamConfig(this.baseUrl, environment, clientId, clientSecret, Some(allowedHeaders))

  }

}
