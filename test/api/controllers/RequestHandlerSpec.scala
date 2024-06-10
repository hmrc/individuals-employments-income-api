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

package api.controllers

import api.controllers.validators.Validator
import api.hateoas.HateoasLinksFactory
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.{ErrorWrapper, MtdError, NinoFormatError}
import api.models.hateoas.{HateoasData, HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import config.AppConfig
import mocks.MockAppConfig
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{JsString, Json, OWrites}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.{FakeRequest, ResultExtractors}
import routing.Version3
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class RequestHandlerSpec
    extends UnitSpec
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator
    with Status
    with HeaderNames
    with ResultExtractors
    with ControllerSpecHateoasSupport
    with MockAppConfig {

  private implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  private val successResponseJson = Json.obj("result" -> "SUCCESS!")
  private val successCode         = ACCEPTED

  private val generatedCorrelationId = "generatedCorrelationId"
  private val serviceCorrelationId   = "serviceCorrelationId"

  MockIdGenerator.generateCorrelationId.returns(generatedCorrelationId).anyNumberOfTimes()

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "SomeController", endpointName = "someEndpoint")

  private val versionHeader = HeaderNames.ACCEPT -> "application/vnd.hmrc.3.0+json"

  implicit val hc: HeaderCarrier   = HeaderCarrier()
  implicit val ctx: RequestContext = RequestContext.from(mockIdGenerator, endpointLogContext)

  private val userDetails = UserDetails("mtdId", "Individual", Some("agentReferenceNumber"))

  implicit val userRequest: UserRequest[AnyContent] = {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)
    UserRequest[AnyContent](userDetails, fakeRequest)
  }

  implicit val appConfig: AppConfig = mockAppConfig
  private val mockService           = mock[DummyService]

  private def service =
    (mockService.service(_: Input.type)(_: RequestContext, _: ExecutionContext)).expects(Input, *, *).anyNumberOfTimes()

  case object Input

  case object Output {
    implicit val writes: OWrites[Output.type] = _ => successResponseJson
  }

  case object HData extends HateoasData

  implicit object HLinksFactory extends HateoasLinksFactory[Output.type, HData.type] {
    override def links(appConfig: AppConfig, data: HData.type): Seq[Link] = hateoaslinks
  }

  trait DummyService {
    def service(input: Input.type)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Output.type]]
  }

  private val successValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Valid(Input)
  }

  private val singleErrorValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Invalid(List(NinoFormatError))
  }

  "RequestHandler" when {

    "given a request" must {
      "return the correct response" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe successResponseJson
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe successCode
      }

      "return no content if required" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withNoContentResult()

        service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

        val result = requestHandler.handleRequest()

        contentAsString(result) shouldBe ""
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NO_CONTENT
      }

      "wrap the response with hateoas links if required§" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withHateoasResult(mockHateoasFactory)(HData, successCode)

        service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

        MockHateoasFactory.wrap(Output, HData) returns HateoasWrapper(Output, hateoaslinks)

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe successResponseJson ++ hateoaslinksJson
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe successCode
      }
    }

    "given a request with a RequestCannotBeFulfilled gov-test-scenario header" when {
      val gtsHeaders = List(
        "gov-test-scenario" -> "REQUEST_CANNOT_BE_FULFILLED",
        "Gov-Test-Scenario" -> "REQUEST_CANNOT_BE_FULFILLED",
        "GOV-TEST-SCENARIO" -> "REQUEST_CANNOT_BE_FULFILLED"
      )

      "allowed in config" should {
        "return RuleRequestCannotBeFulfilled error" in {
          val requestHandler = RequestHandler
            .withValidator(successValidatorForRequest)
            .withService(mockService.service)
            .withNoContentResult()

          MockedAppConfig.allowRequestCannotBeFulfilledHeader(Version3).returns(true).anyNumberOfTimes()

          val expectedContent = Json.parse(
            """
              |{
              |  "code":"RULE_REQUEST_CANNOT_BE_FULFILLED",
              |  "message":"Custom (will vary in production depending on the actual error)"
              |}
              |""".stripMargin
          )

          for (gtsHeader <- gtsHeaders) {

            val userRequest2 = UserRequest[AnyContent](userDetails, FakeRequest().withHeaders(versionHeader, gtsHeader))
            val result       = requestHandler.handleRequest()(ctx, userRequest2, implicitly[ExecutionContext], mockAppConfig)

            status(result) shouldBe 422
            header("X-CorrelationId", result) shouldBe Some(generatedCorrelationId)
            contentAsJson(result) shouldBe expectedContent
          }
        }
      }

      "not allowed in config" should {
        "return success response, as the Gov-Test-Scenario should be ignored" in {
          val requestHandler = RequestHandler
            .withValidator(successValidatorForRequest)
            .withService(mockService.service)
            .withPlainJsonResult(successCode)

          service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

          MockedAppConfig.allowRequestCannotBeFulfilledHeader(Version3).returns(false).anyNumberOfTimes()

          val ctx2: RequestContext = ctx.copy(hc = hc.copy(otherHeaders = List("gov-test-scenario" -> "REQUEST_CANNOT_BE_FULFILLED")))

          val result = requestHandler.handleRequest()(ctx2, userRequest, ec, mockAppConfig)

          contentAsJson(result) shouldBe successResponseJson
          header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
          status(result) shouldBe successCode
        }
      }
    }

    "a request fails with validation errors" must {
      "return the errors" in {
        val requestHandler = RequestHandler
          .withValidator(singleErrorValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header("X-CorrelationId", result) shouldBe Some(generatedCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "a request fails with service errors" must {
      "return the errors" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        service returns Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError)))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "auditing is configured" when {
      val params    = Map("param" -> "value")
      val auditType = "type"
      val txName    = "txName"

      val requestBody = Some(JsString("REQUEST BODY"))

      def auditHandler(includeResponse: Boolean = false): AuditHandler = AuditHandler(
        mockAuditService,
        auditType = auditType,
        transactionName = txName,
        params = params,
        requestBody = requestBody,
        includeResponse = includeResponse
      )

      val basicRequestHandler = RequestHandler
        .withValidator(successValidatorForRequest)
        .withService(mockService.service)
        .withPlainJsonResult(successCode)

      val basicErrorRequestHandler = RequestHandler
        .withValidator(singleErrorValidatorForRequest)
        .withService(mockService.service)
        .withPlainJsonResult(BAD_REQUEST)

      def verifyAudit(correlationId: String, auditResponse: AuditResponse): CallHandler[Future[AuditResult]] =
        MockedAuditService.verifyAuditEvent(
          AuditEvent(
            auditType = auditType,
            transactionName = txName,
            GenericAuditDetail(
              userDetails,
              params = params,
              request = requestBody,
              `X-CorrelationId` = correlationId,
              response = auditResponse)
          ))

      "a request is successful" when {
        "no response is to be audited" must {
          "audit without the response" in {
            val requestHandler = basicRequestHandler.withAuditing(auditHandler())

            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            status(result) shouldBe successCode

            verifyAudit(serviceCorrelationId, AuditResponse(successCode, Right(None)))
          }
        }

        "the response is to be audited" must {
          "audit with the response" in {
            val requestHandler = basicRequestHandler.withAuditing(auditHandler(includeResponse = true))

            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            status(result) shouldBe successCode

            verifyAudit(serviceCorrelationId, AuditResponse(successCode, Right(Some(successResponseJson))))
          }
        }
      }

      "a request fails with validation errors" must {
        "audit the failure" in {
          val requestHandler = basicErrorRequestHandler.withAuditing(auditHandler())

          val result = requestHandler.handleRequest()

          contentAsJson(result) shouldBe NinoFormatError.asJson
          header("X-CorrelationId", result) shouldBe Some(generatedCorrelationId)
          status(result) shouldBe NinoFormatError.httpStatus

          verifyAudit(generatedCorrelationId, AuditResponse(NinoFormatError.httpStatus, Left(List(AuditError(NinoFormatError.code)))))
        }
      }

      "a request fails with service errors" must {
        "audit the failure" in {
          val requestHandler = basicRequestHandler.withAuditing(auditHandler())

          service returns Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError)))

          val result = requestHandler.handleRequest()

          contentAsJson(result) shouldBe NinoFormatError.asJson
          header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
          status(result) shouldBe NinoFormatError.httpStatus

          verifyAudit(
            serviceCorrelationId,
            AuditResponse(NinoFormatError.httpStatus, Left(List(AuditError(NinoFormatError.code))))
          )
        }
      }
    }
  }

}
