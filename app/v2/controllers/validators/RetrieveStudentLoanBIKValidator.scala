package v2.controllers.validators

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.EmploymentsAppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum, ResolverSupport}
import shared.models.errors.MtdError
import v2.controllers.validators.resolvers.ResolveEmploymentId
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest

class RetrieveStudentLoanBIKValidator (nino: String, taxYear: String, employmentId: String, appConfig: EmploymentsAppConfig)
  extends Validator[RetrieveStudentLoanBIKRequest]
    with ResolverSupport {

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.studentLoansMinimumPermittedTaxYear).resolver

  override def validate: Validated[Seq[MtdError], RetrieveStudentLoanBIKRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveEmploymentId(employmentId)
    ).mapN(RetrieveStudentLoanBIKRequest)

}
