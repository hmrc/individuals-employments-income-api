/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.controllers.validators

import cats.data.Validated
import cats.implicits._
import config.EmploymentsAppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum, ResolverSupport}
import shared.models.errors.MtdError
import v2.controllers.validators.resolvers.ResolveEmploymentId
import v2.models.request.deleteStudentLoanBIK.DeleteStudentLoanBIKRequest

class DeleteStudentLoanBIKValidator(nino: String, taxYear: String, employmentId: String, appConfig: EmploymentsAppConfig)
    extends Validator[DeleteStudentLoanBIKRequest]
    with ResolverSupport {

  private val resolveTaxYear =
    ResolveTaxYearMinimum(appConfig.studentLoanMinimumPermittedTaxYear).resolver

  override def validate: Validated[Seq[MtdError], DeleteStudentLoanBIKRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveEmploymentId(employmentId)
    ).mapN(DeleteStudentLoanBIKRequest)

}
