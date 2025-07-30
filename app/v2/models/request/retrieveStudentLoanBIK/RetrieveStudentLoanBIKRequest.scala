package v2.models.request.retrieveStudentLoanBIK

import common.models.domain.EmploymentId
import shared.models.domain.{Nino, TaxYear}

case class RetrieveStudentLoanBIKRequest(nino: Nino, taxYear: TaxYear, employmentId: EmploymentId)