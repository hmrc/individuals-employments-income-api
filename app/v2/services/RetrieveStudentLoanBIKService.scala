package v2.services

import cats.implicits._
import common.errors.{EmploymentIdFormatError, RuleOutsideAmendmentWindowError}
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.connectors.RetrieveStudentLoanBIKConnector
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class RetrieveStudentLoanBIKService @Inject() (connector: RetrieveStudentLoanBIKConnector) extends BaseService {

  def retrieve(request: RetrieveStudentLoanBIKRequest)(implicit
                                                              ctx: RequestContext,
                                                              ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.retrieveStudentLoanBIK(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  }

  private val downstreamErrorMap: Map[String, MtdError] = Map(
    "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
    "INVALID_TAX_YEAR" -> TaxYearFormatError,
    "INVALID_EMPLOYMENT_ID" -> EmploymentIdFormatError,
    "INVALID_CORRELATION_ID" -> InternalError,
    "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError,
    "NOT_FOUND" -> NotFoundError,
    "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
    "OUTSIDE_AMENDMENT_WINDOW" -> RuleOutsideAmendmentWindowError,
    "SERVER_ERROR" -> InternalError,
    "SERVICE_UNAVAILABLE" -> InternalError
  )

}
