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
import common.errors.EmploymentIdFormatError
import common.support.EmploymentsIBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services._
import v1.fixtures.RetrieveEmploymentControllerFixture._

class RetrieveEmploymentControllerIfsISpec extends EmploymentsIBaseSpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1645.enabled" -> false) ++ super.servicesConfig

  private trait Test {

    val nino: String                    = "AA123456A"
    val taxYear: String                 = "2019-20"
    val employmentId: String            = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val ifsEmploymentId: Option[String] = Some("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

    val ifsHmrcEnteredResponseWithoutDateIgnored: JsValue = hmrcEnteredResponseWithoutDateIgnored
    val ifsHmrcEnteredResponseWithDateIgnored: JsValue    = hmrcEnteredResponseWithDateIgnored
    val ifsCustomEnteredResponse: JsValue                 = customEnteredResponse

    def uri: String = s"/$nino/$taxYear/$employmentId"

    def ifsUri: String = s"/income-tax/income/employments/$nino/$taxYear"

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

  "Calling the 'retrieve employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made to retrieve hmrc entered employment with no date ignored present" in new Test {

        val ifsQueryParam: Map[String, String] = Map("employmentId" -> ifsEmploymentId.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParam, OK, ifsHmrcEnteredResponseWithoutDateIgnored)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdHmrcEnteredResponseWithNoDateIgnored
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 200 status code" when {
      "any valid request is made to retrieve hmrc entered employment with date ignored present" in new Test {

        val ifsQueryParam: Map[String, String] = Map("employmentId" -> ifsEmploymentId.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParam, OK, ifsHmrcEnteredResponseWithDateIgnored)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdHmrcEnteredResponseWithDateIgnored
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 200 status code" when {
      "any valid request is made to retrieve custom entered employment" in new Test {

        val ifsQueryParam: Map[String, String] = Map("employmentId" -> ifsEmploymentId.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParam, OK, ifsCustomEnteredResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdCustomEnteredResponse
        response.header("Content-Type") shouldBe Some("application/json")
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

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
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

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", BAD_REQUEST, EmploymentIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
