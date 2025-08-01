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

package v1.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import v1.models.request.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsRequest
import v1.models.response.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsResponse
import v1.services.RetrieveEmploymentAndFinancialDetailsService

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveEmploymentAndFinancialDetailsService extends TestSuite with MockFactory {

  val mockRetrieveEmploymentAndFinancialDetailsService: RetrieveEmploymentAndFinancialDetailsService =
    mock[RetrieveEmploymentAndFinancialDetailsService]

  object MockRetrieveEmploymentAndFinancialDetailsService {

    def retrieve(requestData: RetrieveEmploymentAndFinancialDetailsRequest)
        : CallHandler[Future[ServiceOutcome[RetrieveEmploymentAndFinancialDetailsResponse]]] = {
      (
        mockRetrieveEmploymentAndFinancialDetailsService
          .retrieve(_: RetrieveEmploymentAndFinancialDetailsRequest)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
