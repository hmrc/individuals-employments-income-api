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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.MtdError
import config.AppConfig
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validation.CustomEmploymentDateValidation
import v1.models.request.amendCustomEmployment.{AmendCustomEmploymentRawData, AmendCustomEmploymentRequestBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendCustomEmploymentValidator @Inject() (implicit currentDateTime: CurrentDateTime, appConfig: AppConfig)
    extends Validator[AmendCustomEmploymentRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendCustomEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendCustomEmploymentRawData => List[List[MtdError]] = (data: AmendCustomEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      EmploymentIdValidation.validate(data.employmentId)
    )
  }

  private def parameterRuleValidation: AmendCustomEmploymentRawData => List[List[MtdError]] = (data: AmendCustomEmploymentRawData) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear, appConfig.minimumPermittedTaxYear),
      if (data.temporalValidationEnabled) TaxYearNotEndedValidation.validate(data.taxYear) else Nil
    )
  }

  private def bodyFormatValidator: AmendCustomEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendCustomEmploymentRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: AmendCustomEmploymentRawData => List[List[MtdError]] = (data: AmendCustomEmploymentRawData) => {
    val dataModel: AmendCustomEmploymentRequestBody = data.body.json.as[AmendCustomEmploymentRequestBody]

    List(
      EmployerRefValidation.validateOptional(dataModel.employerRef),
      EmployerNameValidation.validateCustomEmployment(dataModel.employerName),
      CustomEmploymentDateValidation.validate(dataModel.startDate, dataModel.cessationDate, data.taxYear),
      PayrollIdValidation.validateOptional(dataModel.payrollId)
    )
  }

}
