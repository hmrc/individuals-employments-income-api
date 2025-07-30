package v2.connectors

import common.connectors.EmploymentsConnectorSpec
import common.models.domain.{EmploymentId, MtdSourceEnum}
import config.MockEmploymentsAppConfig
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.fixtures.RetrieveNonPayeEmploymentControllerFixture.responseModel
import v2.models.request.deleteStudentLoansBIK.DeleteStudentLoansBIKRequest
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse

import scala.concurrent.Future

class RetrieveStudentLoanBIKConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  "RetrieveStudentLoanBIKContnectorSpec" should{
      "return a 200 for success scenario" in new HipTest with Test {

        willGet(url"$baseUrl/income-tax/income/employments/non-paye/$nino/2018-19?view=LATEST")
          .returns(Future.successful(outcome))

        val result = await(connector.get(request))
        result shouldBe outcome

      }
    }

    "retrieveUkDividendsIncomeAnnualSummary is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new MockEmploymentsAppConfig with IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        willGet(url"$baseUrl/income-tax/income/employments/non-paye/23-24/$nino?view=LATEST")
          .returns(Future.successful(outcome))

        await(connector.retrieveStudentLoanBIK(request)) shouldBe outcome
      }
    }
  }



  trait Test { _: ConnectorSpec =>

    val connector: RetrieveStudentLoanBIKConnector = new RetrieveStudentLoanBIKConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: DeleteStudentLoansBIKRequest =
      DeleteStudentLoansBIKRequest(
        nino = Nino(nino),
        taxYear = TaxYear.fromMtd(taxYear),
        employmentId = EmploymentId(employmentId)
      )

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

  }



}
//  trait Test extends EmploymentsConnectorTest { _: ConnectorTest with MockEmploymentsAppConfig =>
//    def taxYear: TaxYear
//
//    protected val connector: RetrieveNonPayeEmploymentConnector =
//      new RetrieveNonPayeEmploymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig, employmentsAppConfig = mockEmploymentsConfig)
//
//    protected val request: RetrieveNonPayeEmploymentIncomeRequest =
//      RetrieveNonPayeEmploymentIncomeRequest(Nino("AA111111A"), taxYear = taxYear, MtdSourceEnum.latest)
//
//  }