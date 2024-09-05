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
import play.api.http.{HeaderNames, MimeTypes, Status}
import shared.config.MockAppConfig
import shared.mocks.MockHttpClient
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait EmploymentsConnectorSpec extends UnitSpec with Status with MimeTypes with HeaderNames with MockFeatureSwitches {

  lazy val baseUrl = "http://test-BaseUrl"

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  implicit val hc: HeaderCarrier     = HeaderCarrier()
  implicit val ec: ExecutionContext  = scala.concurrent.ExecutionContext.global

  val otherHeaders: Seq[(String, String)] = List(
    "Gov-Test-Scenario" -> "DEFAULT",
    "AnotherHeader"     -> "HeaderValue"
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
    "Environment"   -> "des-environment",
    "Authorization" -> s"Bearer des-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredIfsHeaders: Seq[(String, String)] = List(
    "Environment"   -> "ifs-environment",
    "Authorization" -> s"Bearer ifs-token",
    "CorrelationId" -> s"$correlationId"
  )

  val requiredTysIfsHeaders: Seq[(String, String)] = List(
    "Environment"   -> "TYS-IFS-environment",
    "Authorization" -> s"Bearer TYS-IFS-token",
    "CorrelationId" -> s"$correlationId"
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

  protected trait ConnectorTest extends MockHttpClient with MockAppConfig with MockEmploymentsAppConfig {
    protected val baseUrl: String = "http://test-BaseUrl"

    implicit protected val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

    protected val requiredHeaders: Seq[(String, String)]
    protected def excludedHeaders: Seq[(String, String)] = Seq("AnotherHeader" -> "HeaderValue")

    protected def willGet[T](url: String, parameters: Seq[(String, String)] = List()): CallHandler[Future[T]] =
      MockedHttpClient
        .get(
          url = url,
          parameters = parameters,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        )

    protected def willPost[BODY, T](url: String, body: BODY): CallHandler[Future[T]] =
      MockedHttpClient
        .post(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = excludedHeaders
        )

    protected def willPut[BODY, T](url: String, body: BODY): CallHandler[Future[T]] =
      MockedHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = body,
          requiredHeaders = requiredHeaders ++ Seq("Content-Type" -> "application/json"),
          excludedHeaders = excludedHeaders
        )

    protected def willPut[T](url: String): CallHandler[Future[T]] =
      MockedHttpClient
        .put(
          url = url,
          config = dummyHeaderCarrierConfig,
          body = "",
          excludedHeaders = excludedHeaders
        )

    protected def willDelete[T](url: String): CallHandler[Future[T]] =
      MockedHttpClient
        .delete(
          url = url,
          config = dummyHeaderCarrierConfig,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        )

  }

  protected trait DesTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredDesHeaders

    MockEmploymentsAppConfig.mockAppConfig.desDownstreamConfig.baseUrl shouldBe this.baseUrl
    MockEmploymentsAppConfig.mockAppConfig.desDownstreamConfig.token shouldBe "des-token"
    MockEmploymentsAppConfig.mockAppConfig.desDownstreamConfig.env shouldBe "des-environment"
    MockEmploymentsAppConfig.mockAppConfig.desDownstreamConfig.environmentHeaders shouldBe Some(allowedDesHeaders)
  }

  protected trait IfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredIfsHeaders

    MockEmploymentsAppConfig.mockAppConfig.ifsDownstreamConfig.baseUrl shouldBe this.baseUrl
    MockEmploymentsAppConfig.mockAppConfig.ifsDownstreamConfig.token shouldBe "ifs-token"
    MockEmploymentsAppConfig.mockAppConfig.ifsDownstreamConfig.env shouldBe "ifs-environment"
    MockEmploymentsAppConfig.mockAppConfig.ifsDownstreamConfig.environmentHeaders shouldBe Some(allowedIfsHeaders)
  }

  protected trait Release6Test extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredRelease6Headers

    MockEmploymentsAppConfig.release6DownstreamConfig.baseUrl shouldBe this.baseUrl
    MockEmploymentsAppConfig.release6DownstreamConfig.token shouldBe "release6-token"
    MockEmploymentsAppConfig.release6DownstreamConfig.env shouldBe "release6-environment"
    MockEmploymentsAppConfig.release6DownstreamConfig.environmentHeaders shouldBe Some(allowedIfsHeaders)
  }

  protected trait Api1661Test extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredApi1661Headers

    MockEmploymentsAppConfig.api1661DownstreamConfig.baseUrl shouldBe this.baseUrl
    MockEmploymentsAppConfig.api1661DownstreamConfig.token shouldBe "api1661-token"
    MockEmploymentsAppConfig.api1661DownstreamConfig.env shouldBe "api1661-environment"
    MockEmploymentsAppConfig.api1661DownstreamConfig.environmentHeaders shouldBe Some(allowedIfsHeaders)
  }

  protected trait TysIfsTest extends ConnectorTest {

    protected lazy val requiredHeaders: Seq[(String, String)] = requiredTysIfsHeaders

    MockEmploymentsAppConfig.mockAppConfig.tysIfsDownstreamConfig.baseUrl shouldBe this.baseUrl
    MockEmploymentsAppConfig.mockAppConfig.tysIfsDownstreamConfig.token shouldBe "TYS-IFS-token"
    MockEmploymentsAppConfig.mockAppConfig.tysIfsDownstreamConfig.env shouldBe "TYS-IFS-environment"
    MockEmploymentsAppConfig.mockAppConfig.tysIfsDownstreamConfig.environmentHeaders shouldBe Some(allowedIfsHeaders)
  }

}
