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

package v2.controllers

import api.config.AppConfig
import api.controllers.*
import api.routing.Version
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import api.utils.{IdGenerator, Logging}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import v2.controllers.validators.IgnoreEmploymentValidatorFactory
import v2.services.IgnoreEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IgnoreEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                            val lookupService: MtdIdLookupService,
                                            validatorFactory: IgnoreEmploymentValidatorFactory,
                                            service: IgnoreEmploymentService,
                                            auditService: AuditService,
                                            cc: ControllerComponents,
                                            val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc)
    with Logging {

  val endpointName = "ignore-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "IgnoreEmploymentController",
      endpointName = "ignoreEmployment"
    )

  def ignoreEmployment(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.ignoreEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "IgnoreEmployment",
          apiVersion = Version(request),
          transactionName = "ignore-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
