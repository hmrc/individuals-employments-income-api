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

package auth

import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.auth.AuthSupportingAgentsAllowedISpec
import shared.services.DownstreamStub

class EmploymentsIncomeAuthSupportingAgentsAllowedISpec extends AuthSupportingAgentsAllowedISpec {

  val callingApiVersion = "2.0"

  val supportingAgentsAllowedEndpoint = "amend-custom-employment"

  val taxYear: String = "2019-20"

  private val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val mtdUrl = s"/$nino/$taxYear/$employmentId"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.withQueryStringParameters("taxYear" -> taxYear).put(requestJson))

  val downstreamUri: String = s"/itsd/income/employments/$nino/custom/$employmentId"

  val maybeDownstreamResponseJson: Option[JsValue] = None

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.PUT

  override protected val downstreamSuccessStatus: Int = NO_CONTENT

  override def servicesConfig: Map[String, Any] =
    Map[String, Any](
      "microservice.services.release6.host" -> mockHost,
      "microservice.services.release6.port" -> mockPort
    ) ++ super.servicesConfig

  private val requestJson = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

}
