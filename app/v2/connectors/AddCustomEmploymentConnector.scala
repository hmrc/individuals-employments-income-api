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

package v2.connectors

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.HipUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.*
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.models.request.addCustomEmployment.AddCustomEmploymentRequest
import v2.models.response.addCustomEmployment.AddCustomEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddCustomEmploymentConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def addEmployment(request: AddCustomEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[AddCustomEmploymentResponse]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    val downstreamUri =
      HipUri[AddCustomEmploymentResponse](s"itsd/income/employments/$nino/custom?taxYear=${taxYear.asTysDownstream}")

    post(request.body, downstreamUri)
  }

}
