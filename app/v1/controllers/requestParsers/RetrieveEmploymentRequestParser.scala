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

package v1.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import api.models.domain.{EmploymentId, Nino, TaxYear}
import v1.controllers.requestParsers.validators.RetrieveEmploymentValidator
import v1.models.request.retrieveEmployment.{RetrieveEmploymentRawData, RetrieveEmploymentRequest}

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveEmploymentRequestParser @Inject() (val validator: RetrieveEmploymentValidator)
    extends RequestParser[RetrieveEmploymentRawData, RetrieveEmploymentRequest] {

  override protected def requestFor(data: RetrieveEmploymentRawData): RetrieveEmploymentRequest =
    RetrieveEmploymentRequest(Nino(data.nino), TaxYear.fromMtd(data.taxYear), EmploymentId(data.employmentId))

}
