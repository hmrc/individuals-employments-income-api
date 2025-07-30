package v2.models.response.retrieveStudentLoanBIK

import play.api.libs.json.{Json, OFormat}
import shared.models.domain.Timestamp

class RetrieveStudentLoanBIKResponse (submittedOn: Timestamp,
                                      payrolledBenefits: BigDecimal) {

}

object retrieveStudentLoanBIKResponse {

  implicit val format: OFormat[RetrieveStudentLoanBIKResponse] = Json.format[RetrieveStudentLoanBIKResponse]
}