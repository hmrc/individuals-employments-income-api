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

package v1.connectors

import config.EmploymentsAppConfig
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.TaxYearSpecificIfsUri
import shared.connectors._
import shared.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v1.models.request.deleteEmploymentFinancialDetails.DeleteEmploymentFinancialDetailsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteEmploymentFinancialDetailsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig, employmentsAppConfig: EmploymentsAppConfig) extends BaseDownstreamConnector {

  def deleteEmploymentFinancialDetails(request: DeleteEmploymentFinancialDetailsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](
        s"income-tax/${taxYear.asTysDownstream}/income/employments/$nino/${employmentId.value}"
      )
    } else {
      DownstreamUri[Unit](
        s"income-tax/income/employments/$nino/${taxYear.asMtd}/${employmentId.value}",
        DownstreamStrategy.standardStrategy(employmentsAppConfig.release6DownstreamConfig)
      )
    }

    delete(downstreamUri)

  }

}
