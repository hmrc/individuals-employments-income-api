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
import common.errors.{EmploymentIdFormatError, RuleDeleteForbiddenError, RuleOutsideAmendmentWindowError}
import common.support.EmploymentsIBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class DeleteCustomEmploymentControllerHipISpec extends EmploymentsIBaseSpec {

  private trait Test {

    val nino: String         = "AA123456A"
    val taxYear: String      = "2019-20"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def uri: String = s"/$nino/$taxYear/$employmentId"

    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> "19-20")

    def downstreamUri: String = s"/itsd/income/employments/$nino/custom/$employmentId"


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

  "Calling the 'delete a custom employment' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, downstreamQueryParams, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String         = requestNino
            override val taxYear: String      = requestTaxYear
            override val employmentId: String = requestEmploymentId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20122", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2019-20", "ABCDE12345FG", BAD_REQUEST, EmploymentIdFormatError),
          ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError)
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
             |[
             |  {
             |   "errorCode": "$code",
             |   "errorDescription": "message"
             |  }
             |]
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1217", BAD_REQUEST, EmploymentIdFormatError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError) ,
          (UNPROCESSABLE_ENTITY, "1222", BAD_REQUEST, RuleDeleteForbiddenError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
