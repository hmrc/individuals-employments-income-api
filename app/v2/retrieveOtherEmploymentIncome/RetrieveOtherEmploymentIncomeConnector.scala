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

package v2.retrieveOtherEmploymentIncome

import api.config.AppConfig
import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.retrieveOtherEmploymentIncome.model.request.RetrieveOtherEmploymentIncomeRequest
import v2.retrieveOtherEmploymentIncome.model.response.RetrieveOtherEmploymentIncomeResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveOtherEmploymentIncomeConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveOtherEmploymentIncome(request: RetrieveOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveOtherEmploymentIncomeResponse]] = {

    val resolvedDownstreamUri = if (request.taxYear.useTaxYearSpecificApi) {
      IfsUri[RetrieveOtherEmploymentIncomeResponse](s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
    } else {
      IfsUri[RetrieveOtherEmploymentIncomeResponse](s"income-tax/${request.taxYear.asMtd}/income/other/employments/${request.nino}")
    }

    get(uri = resolvedDownstreamUri)
  }

}
