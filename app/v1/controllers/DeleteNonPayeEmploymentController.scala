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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import config.EmploymentsAppConfig
import shared.controllers.{EndpointLogContext, RequestContext}
import shared.utils.IdGenerator
import v1.controllers.validators.DeleteNonPayeEmploymentIncomeValidatorFactory
import v1.services.DeleteNonPayeEmploymentService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeleteNonPayeEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                   val lookupService: MtdIdLookupService,
                                                   validatorFactory: DeleteNonPayeEmploymentIncomeValidatorFactory,
                                                   service: DeleteNonPayeEmploymentService,
                                                   auditService: AuditService,
                                                   cc: ControllerComponents,
                                                   val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: EmploymentsAppConfig)
    extends AuthorisedController(cc) {

  val endpointName = "delete-non-paye-employment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "DeleteNonPayeEmploymentController",
      endpointName = "delete"
    )

  def delete(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.deleteNonPayeEmployment)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "DeleteNonPayeEmploymentIncome",
          transactionName = "delete-non-paye-employment-income",
          params = Map("nino" -> nino, "taxYear" -> taxYear)
        ))

      requestHandler.handleRequest()
    }

}
