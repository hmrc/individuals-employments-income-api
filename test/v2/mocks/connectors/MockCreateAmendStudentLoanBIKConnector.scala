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
import v2.connectors.CreateAmendStudentLoanBIKConnector
import v2.models.request.createAmendStudentLoanBIK.CreateAmendStudentLoanBIKRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendStudentLoanBIKConnector extends TestSuite with MockFactory {

  val mockConnector: CreateAmendStudentLoanBIKConnector = mock[CreateAmendStudentLoanBIKConnector]

  object MockCreateAmendStudentLoanBIKConnector {

    def createAndAmend(requestData: CreateAmendStudentLoanBIKRequest): CallHandler[Future[DownstreamOutcome[Unit]]] =
      (mockConnector
        .createAndAmend(_: CreateAmendStudentLoanBIKRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)

  }

}
