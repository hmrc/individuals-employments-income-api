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
import shared.config.SharedAppConfig
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamStrategy, DownstreamUri}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.addCustomEmployment.AddCustomEmploymentRequest
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddCustomEmploymentConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig, val employmentsAppConfig: EmploymentsAppConfig) extends BaseDownstreamConnector {

  def addEmployment(request: AddCustomEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[AddCustomEmploymentResponse]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    post(
      request.body,
      DownstreamUri[AddCustomEmploymentResponse](s"income-tax/income/employments/$nino/${taxYear.asMtd}/custom",
        DownstreamStrategy.standardStrategy(employmentsAppConfig.api1661DownstreamConfig))
    )
  }

}
