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

import config.EmploymentsFeatureSwitches
import play.api.http.Status.NO_CONTENT
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.{SuccessCode, reads, readsEmpty}
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.otherEmploymentIncome.{DeleteOtherEmploymentIncomeRequest, RetrieveOtherEmploymentIncomeRequest}
import v2.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherEmploymentIncomeConnector @Inject()(val http: HttpClient, val appConfig: SharedAppConfig)(implicit featureSwitches: EmploymentsFeatureSwitches)
    extends BaseDownstreamConnector {

  def deleteOtherEmploymentIncome(request: DeleteOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[Unit](s"income-tax/${request.taxYear.asMtd}/income/other/employments/${request.nino}")
      } else {
        DesUri[Unit](s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}")
      }

    delete(
      uri = downstreamUri
    )
  }

  def retrieveOtherEmploymentIncome(request: RetrieveOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveOtherEmploymentResponse]] = {

    val resolvedDownstreamUri = if (request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveOtherEmploymentResponse](
        s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
    } else if (featureSwitches.isDesIf_MigrationEnabled) {
      IfsUri[RetrieveOtherEmploymentResponse](s"income-tax/${request.taxYear.asMtd}/income/other/employments/${request.nino}")
    } else {
      DesUri[RetrieveOtherEmploymentResponse](s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}")
    }

    get(uri = resolvedDownstreamUri)
  }

}
