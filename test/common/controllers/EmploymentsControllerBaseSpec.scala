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

package common.controllers

import cats.implicits.catsSyntaxValidatedId
import mocks.MockEmploymentsAppConfig
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import shared.config.Deprecation.NotDeprecated
import shared.controllers.{AuthorisedController, ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse}

import scala.concurrent.Future

trait EmploymentsControllerBaseSpec extends ControllerBaseSpec with MockEmploymentsAppConfig {

  lazy val fakeDeleteRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )

  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

}

trait EmploymentsControllerTestRunner extends ControllerTestRunner {
  _: EmploymentsControllerBaseSpec =>

  trait EmploymentsControllerTest extends ControllerTest {

    protected val controller: AuthorisedController

    protected def callController(): Future[Result]

    MockedEmploymentsAppConfig
      .deprecationFor(apiVersion)
      .returns(NotDeprecated.valid)
      .anyNumberOfTimes()
  }

  trait EmploymentsAuditEventChecking[DETAIL] extends AuditEventChecking[DETAIL] {
    _: EmploymentsControllerTest =>
    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[DETAIL]
  }

}