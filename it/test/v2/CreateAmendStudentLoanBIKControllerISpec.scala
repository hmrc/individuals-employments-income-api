/*
 * Copyright 2025 HM Revenue & Customs
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

package v2

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.{EmploymentIdFormatError, RuleOutsideAmendmentWindowError}
import common.support.EmploymentsIBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAmendStudentLoanBIKControllerISpec extends EmploymentsIBaseSpec {

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "payrolledBenefits": 20000.01
      |}
      |""".stripMargin
  )

  val invalidBodyJson: JsValue = Json.parse(
    """
      |{
      |  "aField": "aValue"
      |}
       """.stripMargin
  )

  val invalidBodyError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(
      Seq(
        "/payrolledBenefits"
      ))
  )

  val missingFieldsJson: JsValue = Json.parse(
    """
      |{}
     """.stripMargin
  )

  private val invalidPayrolledBenefitsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "payrolledBenefits": -1111.737373
      |}
      |""".stripMargin
  )

  val invalidPayrolledBenefitsError: MtdError = ValueFormatError.copy(paths = Some(Seq("/payrolledBenefits")))

  private trait Test {

    val nino: String              = "AA123456A"
    val taxYear: String           = "2025-26"
    val downstreamTaxYear: String = "25-26"
    val employmentId: String      = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def uri: String = s"/$nino/$taxYear/$employmentId/benefit-in-kind/student-loans"

    def downstreamUri: String = s"/itsa/income-tax/v1/$downstreamTaxYear/student-loan/payrolled-benefits/$nino/$employmentId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the 'create and amend student load bik' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(validRequestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedError: MtdError,
                                expectedErrors: Option[ErrorWrapper],
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedError.code} error${scenario.fold("")(scenario => s" for $scenario scenario")}" in new Test {

            override val nino: String         = requestNino
            override val taxYear: String      = requestTaxYear
            override val employmentId: String = requestEmploymentId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedErrors.fold(Json.toJson(expectedError))(errorWrapper => Json.toJson(errorWrapper))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2025-26", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None, None),
          ("AA123456A", "20122", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None, None),
          ("AA123456A", "2025-26", "ABCDE12345FG", validRequestBodyJson, BAD_REQUEST, EmploymentIdFormatError, None, None),
          (
            "AA123456A",
            "2024-25",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            validRequestBodyJson,
            BAD_REQUEST,
            RuleTaxYearNotSupportedError,
            None,
            None),
          (
            "AA123456A",
            "2024-26",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            validRequestBodyJson,
            BAD_REQUEST,
            RuleTaxYearRangeInvalidError,
            None,
            None),

          // body validation
          (
            "AA123456A",
            "2025-26",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            JsObject.empty,
            BAD_REQUEST,
            RuleIncorrectOrEmptyBodyError,
            None,
            Some("emptyBody")),
          (
            "AA123456A",
            "2025-26",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            invalidBodyJson,
            BAD_REQUEST,
            invalidBodyError,
            None,
            Some("nonsenseBody")),
          (
            "AA123456A",
            "2025-26",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            missingFieldsJson,
            BAD_REQUEST,
            RuleIncorrectOrEmptyBodyError,
            None,
            Some("missingFields")),
          (
            "AA123456A",
            "2025-26",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            invalidPayrolledBenefitsRequestBodyJson,
            BAD_REQUEST,
            invalidPayrolledBenefitsError,
            None,
            Some("invalidPayrolledBenefitsErrorRule"))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |  "origin": "HIP",
             |  "response" : {
             |    "failures": [
             |      {
             |        "type": "$code",
             |        "reason": "message"
             |      }
             |    ]
             |  }
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", BAD_REQUEST, EmploymentIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
