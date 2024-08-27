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

import shared.controllers.RequestHandler
import shared.controllers.AuthorisedController
import api.controllers._
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.EmploymentsAppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.controllers.{EndpointLogContext, RequestContext}
import shared.utils.IdGenerator
import utils.Logging
import v1.controllers.validators.UnignoreEmploymentValidatorFactory
import v1.services.UnignoreEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UnignoreEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              validatorFactory: UnignoreEmploymentValidatorFactory,
                                              service: UnignoreEmploymentService,
                                              auditService: AuditService,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: EmploymentsAppConfig)
    extends AuthorisedController(cc)
    with Logging {

  val endpointName = "unignore-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "UnignoreEmploymentController",
      endpointName = "unignoreEmployment"
    )

  def unignoreEmployment(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.unignoreEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "UnignoreEmployment",
          transactionName = "unignore-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
