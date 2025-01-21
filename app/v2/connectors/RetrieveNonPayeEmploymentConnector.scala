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

package v2.connectors

import shared.config.SharedAppConfig
import config.EmploymentsAppConfig
import shared.connectors.DownstreamUri.TaxYearSpecificIfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamStrategy, DownstreamUri}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest
import v2.models.response.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveNonPayeEmploymentConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig, employmentsAppConfig: EmploymentsAppConfig) extends BaseDownstreamConnector {

  def retrieveNonPayeEmployment(request: RetrieveNonPayeEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveNonPayeEmploymentIncomeResponse]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveNonPayeEmploymentIncomeResponse](
        s"income-tax/income/employments/non-paye/${taxYear.asTysDownstream}/${nino.value}?view=${source.toDesViewString}"
      )
    } else {
      DownstreamUri[RetrieveNonPayeEmploymentIncomeResponse](
        s"income-tax/income/employments/non-paye/${nino.value}/${taxYear.asMtd}?view=${source.toDesViewString}",
        DownstreamStrategy.standardStrategy(employmentsAppConfig.api1661DownstreamConfig)
      )
    }

    get(downstreamUri)

  }

}
