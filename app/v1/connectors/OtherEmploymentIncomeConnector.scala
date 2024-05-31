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

package v1.connectors

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.{AppConfig, FeatureSwitches}
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.otherEmploymentIncome.{DeleteOtherEmploymentIncomeRequest, RetrieveOtherEmploymentIncomeRequest}
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherEmploymentIncomeConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit featureSwitches: FeatureSwitches)
    extends BaseDownstreamConnector {

  def deleteOtherEmploymentIncome(request: DeleteOtherEmploymentIncomeRequest)(implicit
                                                                                 hc: HeaderCarrier,
                                                                                 ec: ExecutionContext,
                                                                                 correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val path = s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}"

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[Unit](path)
      } else {
        DesUri[Unit](path)
      }

    delete(
      uri = downstreamUri
    )
  }

  def retrieveOtherEmploymentIncome(request: RetrieveOtherEmploymentIncomeRequest)(implicit
                                                                                   hc: HeaderCarrier,
                                                                                   ec: ExecutionContext,
                                                                                   correlationId: String): Future[DownstreamOutcome[RetrieveOtherEmploymentResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val path = s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}"

    val resolvedDownstreamUri = if (request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveOtherEmploymentResponse](
        s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
    } else if (featureSwitches.isDesIf_MigrationEnabled) {
      IfsUri[RetrieveOtherEmploymentResponse](path)
    } else {
      DesUri[RetrieveOtherEmploymentResponse](path)
    }

    get(uri = resolvedDownstreamUri)
  }

}
