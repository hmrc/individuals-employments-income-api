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

package v1.models.request.amendCustomEmployment

import play.api.libs.json.{JsString, Json, OWrites, Reads}
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}

case class AmendCustomEmploymentRequestBody(employerRef: Option[String],
                                            employerName: String,
                                            startDate: String,
                                            cessationDate: Option[String],
                                            payrollId: Option[String],
                                            occupationalPension: Boolean)

object AmendCustomEmploymentRequestBody {
  implicit val reads: Reads[AmendCustomEmploymentRequestBody] = Json.reads[AmendCustomEmploymentRequestBody]

  implicit def writes(implicit appConfig: SharedAppConfig): OWrites[AmendCustomEmploymentRequestBody] =
    Json.writes[AmendCustomEmploymentRequestBody].transform { json =>
      (json \ "payrollId").asOpt[String].fold(json) { payrollId =>
        val finalPayrollId: String =
          if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1662")) payrollId.take(35) else payrollId.replace("#", "")

        json + ("payrollId" -> JsString(finalPayrollId))
      }
    }
}
