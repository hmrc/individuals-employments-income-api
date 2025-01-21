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

import config.EmploymentsFeatureSwitches
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.controllers.validators.AddCustomEmploymentValidatorFactory
import v2.services.AddCustomEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AddCustomEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               validatorFactory: AddCustomEmploymentValidatorFactory,
                                               service: AddCustomEmploymentService,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  val endpointName = "add-custom-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AddCustomEmploymentController",
      endpointName = "addEmployment"
    )

  def addEmployment(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        body = request.body,
        temporalValidationEnabled = EmploymentsFeatureSwitches(appConfig.featureSwitchConfig).isTemporalValidationEnabled)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.addEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "AddACustomEmployment",
          apiVersion = Version(request),
          transactionName = "add-a-custom-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body),
          includeResponse = true
        ))
        .withPlainJsonResult()

      requestHandler.handleRequest()

    }

}
