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
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.IdGenerator
import v1.controllers.validators.AmendOtherEmploymentValidatorFactory
import v1.services.AmendOtherEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AmendOtherEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                validatorFactory: AmendOtherEmploymentValidatorFactory,
                                                service: AmendOtherEmploymentService,
                                                auditService: AuditService,
                                                cc: ControllerComponents,
                                                val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "AmendOtherEmploymentController",
      endpointName = "amendOtherEmployment"
    )

  def amendOtherEmployment(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        body = request.body
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.amendOtherEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateAmendOtherEmployment",
          transactionName = "create-amend-other-employment",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = Some(request.body)
        ))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
