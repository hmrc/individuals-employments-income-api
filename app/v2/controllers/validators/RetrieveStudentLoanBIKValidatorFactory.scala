package v2.controllers.validators

import config.EmploymentsAppConfig
import shared.controllers.validators.Validator
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveStudentLoanBIKValidatorFactory @Inject() (appConfig: EmploymentsAppConfig) {

  def validator(nino: String, taxYear: String, employmentId: String): Validator[RetrieveStudentLoanBIKRequest] =
    new RetrieveStudentLoanBIKValidator(nino, taxYear, employmentId, appConfig)

}
