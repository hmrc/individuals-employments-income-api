package v2.fixtures

import play.api.libs.json.{JsValue, Json}

object RetrieveStudentLoanBIKFixture {

  val downstreamResponse: JsValue = Json.parse(
    """
     |{
     |  "submittedOn": "2025-08-24T14:15:22Z",
     |  "payrolledBenefits": 20000.01
     |}
  """
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-08-24T14:15:220Z",
      |  "payrolledBenefits": 20000.01
      |}
  """
  )

}
