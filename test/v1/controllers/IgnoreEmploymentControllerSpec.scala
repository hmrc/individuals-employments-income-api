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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockIgnoreEmploymentRequestParser
import v1.mocks.services.MockIgnoreEmploymentService
import v1.models.request.ignoreEmployment.{IgnoreEmploymentRawData, IgnoreEmploymentRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IgnoreEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockIgnoreEmploymentService
    with MockIgnoreEmploymentRequestParser {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new IgnoreEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockIgnoreEmploymentRequestParser,
      service = mockIgnoreEmploymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.ignoreEmployment(nino, taxYear, employmentId)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "IgnoreEmployment",
        transactionName = "ignore-employment",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

  val rawData: IgnoreEmploymentRawData = IgnoreEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  val requestData: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId
  )

  "IgnoreEmploymentController" should {
    "return OK" when {
      "happy path" in new Test {
        MockIgnoreEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockIgnoreEmploymentService
          .ignoreEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = OK)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockIgnoreEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockIgnoreEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockIgnoreEmploymentService
          .ignoreEmployment(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

}
