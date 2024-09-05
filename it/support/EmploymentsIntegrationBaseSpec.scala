package support

import shared.support.IntegrationBaseSpec

import java.time.LocalDate


trait EmploymentsIntegrationBaseSpec extends IntegrationBaseSpec{
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
