package common.support

import shared.support.IntegrationBaseSpec

import java.time.LocalDate

trait EmploymentsIBaseSpec extends IntegrationBaseSpec {
  override def servicesConfig: Map[String, Any] = Map(
    "microservice.services.des.host"           -> mockHost,
    "microservice.services.des.port"           -> mockPort,
    "microservice.services.ifs.host"           -> mockHost,
    "microservice.services.ifs.port"           -> mockPort,
    "microservice.services.tys-ifs.host"       -> mockHost,
    "microservice.services.tys-ifs.port"       -> mockPort,
    "microservice.services.release6.host"      -> mockHost,
    "microservice.services.release6.port"      -> mockPort,
    "microservice.services.api1661.host"       -> mockHost,
    "microservice.services.api1661.port"       -> mockPort,
    "microservice.services.mtd-id-lookup.host" -> mockHost,
    "microservice.services.mtd-id-lookup.port" -> mockPort,
    "microservice.services.auth.host"          -> mockHost,
    "microservice.services.auth.port"          -> mockPort,
    "auditing.consumer.baseUri.port"           -> mockPort,
    "minimumPermittedTaxYear"                  -> 2020
  )

  def getCurrentTaxYear: String = {
    val currentDate = LocalDate.now()

    val taxYearStartDate: LocalDate = LocalDate.parse(s"${currentDate.getYear}-04-06")

    def fromDesIntToString(taxYear: Int): String = s"${taxYear - 1}-${taxYear.toString.drop(2)}"

    if (currentDate.isBefore(taxYearStartDate)) {
      fromDesIntToString(currentDate.getYear)
    } else {
      fromDesIntToString(currentDate.getYear + 1)
    }
  }
}
