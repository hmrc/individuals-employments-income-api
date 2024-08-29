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
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{EmploymentId, Nino, TaxYear}
import shared.models.errors._
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.controllers.validators.MockAmendFinancialDetailsValidatorFactory
import v1.mocks.services.MockAmendFinancialDetailsService
import v1.models.request.amendFinancialDetails.employment.studentLoans.AmendStudentLoans
import v1.models.request.amendFinancialDetails.employment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAmendFinancialDetailsValidatorFactory
    with MockAmendFinancialDetailsService
    with MockAuditService {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": 13343.45,
      |                "pglDeductionAmount": 24242.56
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": 455.67,
      |            "assets": 435.54,
      |            "assetTransfer": 24.58,
      |            "beneficialLoan": 33.89,
      |            "car": 3434.78,
      |            "carFuel": 34.56,
      |            "educationalServices": 445.67,
      |            "entertaining": 434.45,
      |            "expenses": 3444.32,
      |            "medicalInsurance": 4542.47,
      |            "telephone": 243.43,
      |            "service": 45.67,
      |            "taxableExpenses": 24.56,
      |            "van": 56.29,
      |            "vanFuel": 14.56,
      |            "mileage": 34.23,
      |            "nonQualifyingRelocationExpenses": 54.62,
      |            "nurseryPlaces": 84.29,
      |            "otherItems": 67.67,
      |            "paymentsOnEmployeesBehalf": 67.23,
      |            "personalIncidentalExpenses": 74.29,
      |            "qualifyingRelocationExpenses": 78.24,
      |            "employerProvidedProfessionalSubscriptions": 84.56,
      |            "employerProvidedServices": 56.34,
      |            "incomeTaxPaidByDirector": 67.34,
      |            "travelAndSubsistence": 56.89,
      |            "vouchersAndCreditCards": 34.90,
      |            "nonCash": 23.89
      |        },
      |        "offPayrollWorker": true
      |    }
      |}
    """.stripMargin
  )

  val pay: AmendPay = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92
  )

  val studentLoans: AmendStudentLoans = AmendStudentLoans(
    uglDeductionAmount = Some(13343.45),
    pglDeductionAmount = Some(24242.56)
  )

  val deductions: AmendDeductions = AmendDeductions(
    studentLoans = Some(studentLoans)
  )

  val benefitsInKind: AmendBenefitsInKind = AmendBenefitsInKind(
    accommodation = Some(455.67),
    assets = Some(435.54),
    assetTransfer = Some(24.58),
    beneficialLoan = Some(33.89),
    car = Some(3434.78),
    carFuel = Some(34.56),
    educationalServices = Some(445.67),
    entertaining = Some(434.45),
    expenses = Some(3444.32),
    medicalInsurance = Some(4542.47),
    telephone = Some(243.43),
    service = Some(45.67),
    taxableExpenses = Some(24.56),
    van = Some(56.29),
    vanFuel = Some(14.56),
    mileage = Some(34.23),
    nonQualifyingRelocationExpenses = Some(54.62),
    nurseryPlaces = Some(84.29),
    otherItems = Some(67.67),
    paymentsOnEmployeesBehalf = Some(67.23),
    personalIncidentalExpenses = Some(74.29),
    qualifyingRelocationExpenses = Some(78.24),
    employerProvidedProfessionalSubscriptions = Some(84.56),
    employerProvidedServices = Some(56.34),
    incomeTaxPaidByDirector = Some(67.34),
    travelAndSubsistence = Some(56.89),
    vouchersAndCreditCards = Some(34.90),
    nonCash = Some(23.89)
  )

  val employment: AmendEmployment = AmendEmployment(
    pay = pay,
    deductions = Some(deductions),
    benefitsInKind = Some(benefitsInKind),
    offPayrollWorker = None
  )

  val amendFinancialDetailsRequestBody: AmendFinancialDetailsRequestBody = AmendFinancialDetailsRequestBody(
    employment = employment
  )

  val requestData: AmendFinancialDetailsRequest = AmendFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId),
    body = amendFinancialDetailsRequestBody
  )

  "AmendFinancialDetailsController" should {
    "return OK" when {
      "the request is valid and is handled successfully" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson)
        )
      }
    }

    "return the error as per spec" when {
      "parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    MockedAppConfig.featureSwitches
      .returns(Configuration("allowTemporalValidationSuspension.enabled" -> true))
      .anyNumberOfTimes()

    val controller = new AmendFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendFinancialDetailsValidatorFactory,
      service = mockAmendFinancialDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendEmploymentFinancialDetails",
        transactionName = "amend-employment-financial-details",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] =
      controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

  }

}
