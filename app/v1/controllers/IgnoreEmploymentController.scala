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

package v1.controllers

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.IgnoreEmploymentRequestParser
import v1.models.request.ignoreEmployment.IgnoreEmploymentRawData
import v1.services.IgnoreEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IgnoreEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                            val lookupService: MtdIdLookupService,
                                            requestParser: IgnoreEmploymentRequestParser,
                                            service: IgnoreEmploymentService,
                                            auditService: AuditService,
                                            cc: ControllerComponents,
                                            val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "IgnoreEmploymentController",
      endpointName = "ignoreEmployment"
    )

  def ignoreEmployment(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = IgnoreEmploymentRawData(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      val requestHandler = RequestHandler
        .withParser(requestParser)
        .withService(service.ignoreEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "IgnoreEmployment",
          transactionName = "ignore-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest(rawData)
    }

}
