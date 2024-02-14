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

package api.hateoas

import api.models.hateoas.Link
import api.models.hateoas.Method._
import api.models.hateoas.RelType._
import config.AppConfig

//noinspection ScalaStyle
trait HateoasLinks {

  private def otherEmploymentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/other/$nino/$taxYear"

  private def employmentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/$nino/$taxYear"

  private def employmentUriWithId(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String) =
    s"/${appConfig.apiGatewayContext}/$nino/$taxYear/$employmentId"

  private def nonPayeEmploymentUri(appConfig: AppConfig, nino: String, taxYear: String) =
    s"/${appConfig.apiGatewayContext}/non-paye/$nino/$taxYear"

  // API resource links

  // Other Employment Income
  def amendOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = PUT,
      rel = AMEND_OTHER_EMPLOYMENT_INCOME
    )

  def retrieveOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def deleteOtherEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = otherEmploymentUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_OTHER_EMPLOYMENT_INCOME
    )

  // Employments
  def listEmployment(appConfig: AppConfig, nino: String, taxYear: String, isSelf: Boolean): Link =
    Link(
      href = employmentUri(appConfig, nino, taxYear),
      method = GET,
      rel = if (isSelf) SELF else LIST_EMPLOYMENTS
    )

  def retrieveEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = GET,
      rel = SELF
    )

  def ignoreEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/ignore",
      method = POST,
      rel = IGNORE_EMPLOYMENT
    )

  def unignoreEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/unignore",
      method = POST,
      rel = UNIGNORE_EMPLOYMENT
    )

  // Employment
  def addCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = employmentUri(appConfig, nino, taxYear),
      method = POST,
      rel = ADD_CUSTOM_EMPLOYMENT
    )

  def amendCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = PUT,
      rel = AMEND_CUSTOM_EMPLOYMENT
    )

  def deleteCustomEmployment(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = employmentUriWithId(appConfig, nino, taxYear, employmentId),
      method = DELETE,
      rel = DELETE_CUSTOM_EMPLOYMENT
    )

  // Financial Details
  def retrieveFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = GET,
      rel = SELF
    )

  def amendFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = PUT,
      rel = AMEND_EMPLOYMENT_FINANCIAL_DETAILS
    )

  def deleteFinancialDetails(appConfig: AppConfig, nino: String, taxYear: String, employmentId: String): Link =
    Link(
      href = s"${employmentUriWithId(appConfig, nino, taxYear, employmentId)}/financial-details",
      method = DELETE,
      rel = DELETE_EMPLOYMENT_FINANCIAL_DETAILS
    )

  // Non-PAYE Employment Income
  def retrieveNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = GET,
      rel = SELF
    )

  def createAmendNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = PUT,
      rel = CREATE_AND_AMEND_NON_PAYE_EMPLOYMENT
    )

  def deleteNonPayeEmployment(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = nonPayeEmploymentUri(appConfig, nino, taxYear),
      method = DELETE,
      rel = DELETE_NON_PAYE_EMPLOYMENT
    )

}
