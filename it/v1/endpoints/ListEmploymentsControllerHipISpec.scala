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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec
import v1.fixtures.ListEmploymentsControllerFixture

class ListEmploymentsControllerHipISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String         = "AA123456A"
    val taxYear: String      = "2019-20"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val downstreamQueryParam: Map[String, String] = Map("taxYear" -> "19-20")

    val downstreamResponse: JsValue = Json.parse(
      """
        |{
        |   "employments": [
        |      {
        |         "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |         "employerName": "Vera Lynn",
        |         "employerRef": "123/AZ12334",
        |         "payrollId": "123345657",
        |         "startDate": "2019-01-01",
        |         "cessationDate": "2020-06-01",
        |         "dateIgnored": "2020-06-17T10:53:38Z"
        |      },
        |      {
        |         "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |         "employerName": "Vera Lynn",
        |         "employerRef": "123/AZ12334",
        |         "payrollId": "123345657",
        |         "startDate": "2019-01-01",
        |         "cessationDate": "2020-06-01",
        |         "dateIgnored": "2020-06-17T10:53:38Z"
        |      }
        |   ],
        |   "customerDeclaredEmployments": [
        |      {
        |         "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |         "employerName": "Vera Lynn",
        |         "employerRef": "123/AZ12334",
        |         "payrollId": "123345657",
        |         "startDate": "2019-01-01",
        |         "cessationDate": "2020-06-01",
        |         "submittedOn": "2020-06-17T10:53:38Z"
        |      },
        |      {
        |         "employmentId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |         "employerName": "Vera Lynn",
        |         "employerRef": "123/AZ12334",
        |         "payrollId": "123345657",
        |         "startDate": "2019-01-01",
        |         "cessationDate": "2020-06-01",
        |         "submittedOn": "2020-06-17T10:53:38Z"
        |      }
        |   ]
        |}
    """.stripMargin
    )

    val mtdResponse: JsValue = ListEmploymentsControllerFixture.mtdResponse(employmentId)

    def uri: String = s"/$nino/$taxYear"

    def downstreamUri: String = s"/itsd/income/employments/$nino"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the 'List Employments' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParam, OK, downstreamResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2019-21", BAD_REQUEST, RuleTaxYearRangeInvalidError)
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
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParam, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |[
             |  {
             |    "errorCode": "$code",
             |    "errorDescription": "downstream message"
             |  }
             |]
          """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1217", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
