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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors._
import common.support.EmploymentsIBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AmendCustomEmploymentControllerHipISpec extends EmploymentsIBaseSpec {

  private trait Test {

    val nino: String    = "AA123456A"
    val taxYear: String = "2019-20"
    val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "employerRef": "123/AZ12334",
        |  "employerName": "AMD infotech Ltd",
        |  "startDate": "2019-01-01",
        |  "cessationDate": "2020-06-01",
        |  "payrollId": "124214112412",
        |  "occupationalPension": false
        |}
    """.stripMargin
    )

    def uri: String = s"/$nino/$taxYear/$employmentId"

    def hipUri: String = s"/itsd/income/employments/$nino/custom/$employmentId"

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

  "Calling the amend custom employment endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, hipUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).put(requestBodyJson))
        response.status shouldBe OK
        response.header("Content-Type") shouldBe None
      }
    }

    "return error according to spec" when {

      val validRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val emptyRequestJson: JsValue = JsObject.empty

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": false,
          |  "employerName": false,
          |  "startDate": false,
          |  "cessationDate": false,
          |  "payrollId": false,
          |  "occupationalPension": 77
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334"
          |}
        """.stripMargin
      )

      val invalidEmployerRefRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "notValid",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidEmployerNameRequestJson: JsValue = Json.parse(
        s"""
           |{
           |  "employerRef": "123/AZ12334",
           |  "employerName": "${"a" * 100}",
           |  "startDate": "2019-01-01",
           |  "cessationDate": "2020-06-01",
           |  "payrollId": "124214112412",
           |  "occupationalPension": false
           |}
      """.stripMargin
      )

      val invalidStartDateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "notValid",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidCessationDateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "notValid",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val startDateOutsideAllowedRangeJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "0010-01-01",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val cessationDateOutsideAllowedRangeJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "2220-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidPayrollIdRequestJson: JsValue = Json.parse(
        s"""
           |{
           |  "employerRef": "123/AZ12334",
           |  "employerName": "AMD infotech Ltd",
           |  "startDate": "2019-01-01",
           |  "cessationDate": "2020-06-01",
           |  "payrollId": "${"a" * 100}",
           |  "occupationalPension": false
           |}
      """.stripMargin
      )

      val invalidDateOrderRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2020-01-01",
          |  "cessationDate": "2019-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val startDateLateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2023-01-01",
          |  "cessationDate": "2023-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val cessationDateEarlyRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2018-01-01",
          |  "cessationDate": "2018-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidFieldType: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/cessationDate",
            "/employerName",
            "/employerRef",
            "/occupationalPension",
            "/payrollId",
            "/startDate"
          ))
      )

      val missingMandatoryFieldErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/employerName",
            "/occupationalPension",
            "/startDate"
          ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
            override val employmentId: String     = requestEmploymentId
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def getCurrentTaxYear: String = TaxYear.currentTaxYear.asMtd

        val input = Seq(
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "20177", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", validRequestJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2019-20", "ABCDEFG", validRequestJson, BAD_REQUEST, EmploymentIdFormatError, None),
          ("AA123456A", "2015-17", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2015-16", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", validRequestJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", getCurrentTaxYear, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, RuleTaxYearNotEndedError, None),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", invalidEmployerRefRequestJson, BAD_REQUEST, EmployerRefFormatError, None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            invalidEmployerNameRequestJson,
            BAD_REQUEST,
            EmployerNameFormatError,
            None),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", invalidPayrollIdRequestJson, BAD_REQUEST, PayrollIdFormatError, None),
          ("AA123456A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", invalidStartDateRequestJson, BAD_REQUEST, StartDateFormatError, None),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            invalidCessationDateRequestJson,
            BAD_REQUEST,
            CessationDateFormatError,
            None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            invalidDateOrderRequestJson,
            BAD_REQUEST,
            RuleCessationDateBeforeStartDateError,
            None),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            startDateLateRequestJson,
            BAD_REQUEST,
            RuleStartDateAfterTaxYearEndError,
            None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            cessationDateEarlyRequestJson,
            BAD_REQUEST,
            RuleCessationDateBeforeTaxYearStartError,
            None),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", emptyRequestJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            nonValidRequestBodyJson,
            BAD_REQUEST,
            invalidFieldType,
            Some("(invalid field type)")),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            missingFieldRequestBodyJson,
            BAD_REQUEST,
            missingMandatoryFieldErrors,
            Some("(missing mandatory fields)"))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "validation format error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
            override val employmentId: String     = requestEmploymentId
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", startDateOutsideAllowedRangeJson, BAD_REQUEST, StartDateFormatError, None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            cessationDateOutsideAllowedRangeJson,
            BAD_REQUEST,
            CessationDateFormatError,
            None)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "hip service error" when {
        def serviceErrorTest(hipStatus: Int, hipCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"hip returns an $hipCode error and status $hipStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, hipUri, hipStatus, errorBody(hipCode))
            }

            val response: WSResponse = await(request().withQueryStringParameters("taxYear" -> taxYear).put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |[
             |  {
             |    "errorCode": "$code",
             |    "errorDescription": "string"
             |  }
             |]
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (UNPROCESSABLE_ENTITY, "1115", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "1116", BAD_REQUEST, RuleStartDateAfterTaxYearEndError),
          (UNPROCESSABLE_ENTITY, "1118", BAD_REQUEST, RuleCessationDateBeforeTaxYearStartError),
          (UNPROCESSABLE_ENTITY, "1221", BAD_REQUEST, RuleUpdateForbiddenError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (BAD_REQUEST, "1217", BAD_REQUEST, EmploymentIdFormatError),
          (FORBIDDEN, "1221", BAD_REQUEST, RuleUpdateForbiddenError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}

