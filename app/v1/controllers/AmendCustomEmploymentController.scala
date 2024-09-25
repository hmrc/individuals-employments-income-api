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

import config.EmploymentsFeatureSwitches
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.AppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v1.controllers.validators.AmendCustomEmploymentValidatorFactory
import v1.services.AmendCustomEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendCustomEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 validatorFactory: AmendCustomEmploymentValidatorFactory,
                                                 service: AmendCustomEmploymentService,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  val endpointName = "amend-custom-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendCustomEmploymentController",
      endpointName = "amendCustomEmployment"
    )

  def amendEmployment(nino: String, taxYear: String, employmentId: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId,
        body = request.body,
        temporalValidationEnabled = EmploymentsFeatureSwitches(appConfig.featureSwitchConfig).isTemporalValidationEnabled
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.amendEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "AmendACustomEmployment",
          apiVersion = Version(request),
          transactionName = "amend-a-custom-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          requestBody = Some(request.body)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
