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

package v2.deleteOtherEmploymentIncome

import api.config.AppConfig
import api.connectors.DownstreamUri.IfsUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, readsEmpty}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.deleteOtherEmploymentIncome.model.request.DeleteOtherEmploymentIncomeRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteOtherEmploymentIncomeConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deleteOtherEmploymentIncome(request: DeleteOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        IfsUri[Unit](s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
      } else {
        IfsUri[Unit](s"income-tax/${request.taxYear.asMtd}/income/other/employments/${request.nino}")
      }

    delete(
      uri = downstreamUri
    )
  }

}
