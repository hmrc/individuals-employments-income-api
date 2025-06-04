package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.{EmploymentIdFormatError, RuleCustomEmploymentUnignoreError, RuleOutsideAmendmentWindowError}
import common.support.EmploymentsIBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class UnignoreEmploymentControllerHipISpec extends EmploymentsIBaseSpec {

  private trait Test {

    val nino: String         = "AA123456A"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val taxYear: String      = "2023-24"

    val downstreamQueryParam: Map[String, String] = Map("taxYear" -> "23-24")

    def mtdUri: String = s"/$nino/$taxYear/$employmentId/unignore"

    def downstreamUri: String = s"/itsd/income/ignore/employments/$nino/$employmentId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |    "origin": "HIP",
         |    "response": {
         |        "failures": [
         |            {
         |                "type": "$code",
         |                "reason": "error message"
         |            }
         |        ]
         |    }
         |}
      """.stripMargin

  }

  "Calling the 'unignore employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe OK
        response.header("Content-Type") shouldBe None
      }
    }

    "return a validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestEmploymentId: String,
                              expectedStatus: Int,
                              expectedBody: MtdError,
                              scenario: Option[String]): Unit = {
        s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

          override val nino: String         = requestNino
          override val taxYear: String      = requestTaxYear
          override val employmentId: String = requestEmploymentId

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().post(JsObject.empty))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError, None),
        ("AA123456A", "20199", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, TaxYearFormatError, None),
        ("AA123456A", "2019-20", "ABCDE12345FG", BAD_REQUEST, EmploymentIdFormatError, None),
        ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotSupportedError, None),
        ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError, None)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return a service error according to spec" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamErrorCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamQueryParam, downstreamStatus, errorBody(downstreamErrorCode))
          }

          val response: WSResponse = await(request().post(JsObject.empty))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val errors = List(
        (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "1217", BAD_REQUEST, EmploymentIdFormatError),
        (BAD_REQUEST, "1119", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "1223", BAD_REQUEST, RuleCustomEmploymentUnignoreError),
        (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "1115", BAD_REQUEST, RuleTaxYearNotEndedError),
        (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError),
        (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError)
      )

      errors.foreach(args => (serviceErrorTest _).tupled(args))
    }

  }

}
