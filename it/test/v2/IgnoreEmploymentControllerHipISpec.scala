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

import common.errors.{EmploymentIdFormatError, RuleCustomEmploymentError, RuleOutsideAmendmentWindowError}
import common.support.EmploymentsIBaseSpec
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class IgnoreEmploymentControllerHipISpec extends EmploymentsIBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"

    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    def taxYear: String      = "2019-20"

    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> "19-20")

    def downstreamUri: String = s"/itsd/income/ignore/employments/$nino/$employmentId"

    def setupStubs(): Unit = ()

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/$taxYear/$employmentId/ignore")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  "Calling the 'ignore employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, status = CREATED, JsObject.empty)

        val response: WSResponse = await(request.post(JsObject.empty))
        response.status shouldBe OK
        response.header("Content-Type") shouldBe None
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

            val response: WSResponse = await(request.post(JsObject.empty))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2019-20", "ABCDE12345FG", BAD_REQUEST, EmploymentIdFormatError),
          ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request.post(JsObject.empty))
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

        val errors = Seq(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1217", BAD_REQUEST, EmploymentIdFormatError),
          (BAD_REQUEST, "1119", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "1115", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "1224", BAD_REQUEST, RuleCustomEmploymentError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError)
        )
        errors.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
