/*
 * Copyright 2024 HM Revenue & Customs
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

import config.EmploymentsAppConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import v2.models.request.createAmendStudentLoanBIK.CreateAmendStudentLoanBIKRequest

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendStudentLoanBIKValidatorFactory @Inject()(appConfig: EmploymentsAppConfig) {

  def validator(nino: String, taxYear: String, employmentId: String, body: JsValue): Validator[CreateAmendStudentLoanBIKRequest] =
    new CreateAmendStudentLoanBIKValidator(nino, taxYear, employmentId, body, appConfig)

}
