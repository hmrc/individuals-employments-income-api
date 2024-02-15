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

import api.models.domain.Nino
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockListEmploymentsValidator
import v1.models.request.listEmployments.{ListEmploymentsRawData, ListEmploymentsRequest}

class ListEmploymentsRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2020-21"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val listEmploymentsRawData: ListEmploymentsRawData = ListEmploymentsRawData(
    nino = nino,
    taxYear = taxYear
  )

  trait Test extends MockListEmploymentsValidator {

    lazy val parser: ListEmploymentsRequestParser = new ListEmploymentsRequestParser(
      validator = mockListEmploymentsValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockListEmploymentsValidator.validate(listEmploymentsRawData).returns(Nil)

        parser.parseRequest(listEmploymentsRawData) shouldBe
          Right(ListEmploymentsRequest(Nino(nino), taxYear))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockListEmploymentsValidator
          .validate(listEmploymentsRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(listEmploymentsRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockListEmploymentsValidator
          .validate(listEmploymentsRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(listEmploymentsRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
