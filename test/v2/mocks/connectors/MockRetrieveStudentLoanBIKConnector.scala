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

package v2.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.RetrieveStudentLoanBIKConnector
import v2.models.request.retrieveStudentLoanBIK.RetrieveStudentLoanBIKRequest
import v2.models.response.retrieveStudentLoanBIK.RetrieveStudentLoanBIKResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveStudentLoanBIKConnector extends TestSuite with MockFactory {

val mockRetrieveStudentLoanBIKConnector: RetrieveStudentLoanBIKConnector =
  mock[RetrieveStudentLoanBIKConnector]



  object RetrieveStudentLoanBIKConnector {

    def retrieveStudentLoanBIK(
                                requestData:RetrieveStudentLoanBIKRequest): CallHandler[Future[DownstreamOutcome[RetrieveStudentLoanBIKResponse]]] =
      (
        mockRetrieveStudentLoanBIKConnector
          .retrieveStudentLoanBIK(_: RetrieveStudentLoanBIKRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
      )
        .expects(requestData, *, *, *)
  }
}
