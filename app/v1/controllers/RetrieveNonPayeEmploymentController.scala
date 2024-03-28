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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.RetrieveNonPayeEmploymentRequestParser
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRawData
import v1.services.RetrieveNonPayeEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveNonPayeEmploymentController @Inject() (val authService: EnrolmentsAuthService,
                                                     val lookupService: MtdIdLookupService,
                                                     parser: RetrieveNonPayeEmploymentRequestParser,
                                                     service: RetrieveNonPayeEmploymentService,
                                                     cc: ControllerComponents,
                                                     val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveNonPayeEmploymentController",
      endpointName = "retrieveNonPayeEmployment"
    )

  def retrieveNonPayeEmployment(nino: String, taxYear: String, source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveNonPayeEmploymentIncomeRawData(
        nino = nino,
        taxYear = taxYear,
        source = source
      )

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.retrieveNonPayeEmployment)
          .withPlainJsonResult()

      requestHandler.handleRequest(rawData)
    }

}
