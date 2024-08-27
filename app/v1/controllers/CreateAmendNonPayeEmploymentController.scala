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
import config.EmploymentsFeatureSwitches
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import config.EmploymentsAppConfig
import v1.controllers.validators.CreateAmendNonPayeEmploymentIncomeValidatorFactory
import v1.services.CreateAmendNonPayeEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import shared.controllers.{EndpointLogContext, RequestContext}
import shared.utils.IdGenerator

@Singleton
class CreateAmendNonPayeEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                        val lookupService: MtdIdLookupService,
                                                        validatorFactory: CreateAmendNonPayeEmploymentIncomeValidatorFactory,
                                                        service: CreateAmendNonPayeEmploymentService,
                                                        auditService: AuditService,
                                                        cc: ControllerComponents,
                                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: EmploymentsAppConfig)
    extends AuthorisedController(cc) {

  val endpointName = "create-amend-non-paye-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendNonPayeEmploymentController",
      endpointName = "createAmendNonPayeEmployment"
    )

  def createAmendNonPayeEmployment(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        body = request.body,
        temporalValidationEnabled = EmploymentsFeatureSwitches(appConfig.featureSwitchConfig).isTemporalValidationEnabled
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAndAmend)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendNonPayeEmploymentIncome",
          transactionName = "create-amend-non-paye-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
