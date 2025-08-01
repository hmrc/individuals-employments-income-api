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

import play.api.http.Status.CREATED
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.models.request.ignoreEmployment.IgnoreEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IgnoreEmploymentConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def ignoreEmployment(
      request: IgnoreEmploymentRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1940")) {
      implicit val successCode: SuccessCode = SuccessCode(CREATED)

      val downstreamUri: DownstreamUri[Unit] =
        HipUri[Unit](s"itsd/income/ignore/employments/$nino/${employmentId.value}?taxYear=${taxYear.asTysDownstream}")

      putEmpty(uri = downstreamUri)
    } else {
      val downstreamUri: DownstreamUri[Unit] =
        IfsUri[Unit](s"income-tax/${taxYear.asTysDownstream}/income/employments/$nino/${employmentId.value}/ignore")

      put(uri = downstreamUri, body = "")
    }
  }

}
