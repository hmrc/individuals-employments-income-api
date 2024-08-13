package mocks

import config.{EmploymentsAppConfig, EmploymentsFeatureSwitches}
import org.scalamock.scalatest.MockFactory
import shared.models.domain.TaxYear

trait MockEmploymentsAppConfig extends MockFactory {
  val mockCalculationsConfig: EmploymentsAppConfig = mock[EmploymentsAppConfig]

  object MockEmploymentsAppConfig extends MockAppConfig {
    val minimumPermittedTaxYear: TaxYear = (() => mockCalculationsConfig.minimumPermittedTaxYear: TaxYear).expects()
  }

}
