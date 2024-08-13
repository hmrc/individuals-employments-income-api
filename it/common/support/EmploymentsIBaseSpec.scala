package common.support

import shared.support.IntegrationBaseSpec

trait EmploymentsIBaseSpec extends IntegrationBaseSpec {
  override def servicesConfig: Map[String, Any] =  Map(
    "microservice.services.release6.host"      -> mockHost,
    "microservice.services.release6.port"      -> mockPort,
    "microservice.services.api1661.host"       -> mockHost,
    "microservice.services.api1661.port"       -> mockPort,
  ) ++ super.servicesConfig

}
