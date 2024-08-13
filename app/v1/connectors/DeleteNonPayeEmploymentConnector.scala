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

import config.EmploymentsAppConfig
import shared.connectors.DownstreamUri.TaxYearSpecificIfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamStrategy, DownstreamUri}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.deleteNonPayeEmployment.DeleteNonPayeEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteNonPayeEmploymentConnector @Inject() (val http: HttpClient, val appConfig: EmploymentsAppConfig) extends BaseDownstreamConnector {

  def deleteNonPayeEmployment(request: DeleteNonPayeEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](
        s"income-tax/employments/non-paye/${taxYear.asTysDownstream}/${nino.value}"
      )
    } else {
      DownstreamUri[Unit](
        s"income-tax/employments/non-paye/${nino.value}/${taxYear.asMtd}",
        DownstreamStrategy.standardStrategy(appConfig.api1661DownstreamConfig)
      )
    }

    delete(downstreamUri)

  }

}
