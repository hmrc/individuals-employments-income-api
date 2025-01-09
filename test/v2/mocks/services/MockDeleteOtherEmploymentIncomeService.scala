/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import v2.models.request.otherEmploymentIncome.DeleteOtherEmploymentIncomeRequest
import v2.services.DeleteOtherEmploymentIncomeService

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteOtherEmploymentIncomeService extends MockFactory {

  val mockDeleteOtherEmploymentIncomeService: DeleteOtherEmploymentIncomeService =
    mock[DeleteOtherEmploymentIncomeService]

  object MockDeleteOtherEmploymentIncomeService {

    def delete(requestData: DeleteOtherEmploymentIncomeRequest): CallHandler[Future[ServiceOutcome[Unit]]] = (
      mockDeleteOtherEmploymentIncomeService
        .delete(_: DeleteOtherEmploymentIncomeRequest)(
          _: RequestContext,
          _: ExecutionContext
        )
      )
      .expects(requestData, *, *)

  }

}
