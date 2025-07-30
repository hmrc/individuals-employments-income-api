package v2.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.controllers.validators.RetrieveStudentLoanBIKValidatorFactory
import v2.services.RetrieveStudentLoanBIKService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveStudentLoanBIKController @Inject() (val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  validatorFactory: RetrieveStudentLoanBIKValidatorFactory,
                                                  service: RetrieveStudentLoanBIKService,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  val endpointName = "retrieve-student-loan-benefits-in-kind"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveStudentLoanBIKController",
      endpointName = "retrieveStudentLoanBenefitsInKind"
    )

  def retrieveStudentLoanBIK(nino: String, taxYear: String, employmentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(
        nino = nino,
        taxYear = taxYear,
        employmentId = employmentId
      )

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.retrieve)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "DeleteStudentLoansBenefitsInKind",
          apiVersion = Version(request),
          transactionName = "delete-student-loans-benefits-in-kind",
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId)
        ))
      requestHandler.handleRequest()
    }

  }

