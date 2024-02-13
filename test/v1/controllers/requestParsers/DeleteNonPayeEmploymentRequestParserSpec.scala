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

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockDeleteNonPayeEmploymentValidator
import v1.models.request.deleteNonPayeEmployment.{DeleteNonPayeEmploymentRawData, DeleteNonPayeEmploymentRequest}

class DeleteNonPayeEmploymentRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val deleteNonPayeEmploymentSavingsRawData: DeleteNonPayeEmploymentRawData = DeleteNonPayeEmploymentRawData(
    nino = nino,
    taxYear = taxYear
  )

  trait Test extends MockDeleteNonPayeEmploymentValidator {

    lazy val parser: DeleteNonPayeEmploymentRequestParser = new DeleteNonPayeEmploymentRequestParser(
      validator = mockDeleteNonPayeEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeleteNonPayeEmploymentValidator.validate(deleteNonPayeEmploymentSavingsRawData).returns(Nil)

        parser.parseRequest(deleteNonPayeEmploymentSavingsRawData) shouldBe
          Right(DeleteNonPayeEmploymentRequest(Nino(nino), TaxYear.fromMtd(taxYear)))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteNonPayeEmploymentValidator
          .validate(deleteNonPayeEmploymentSavingsRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(deleteNonPayeEmploymentSavingsRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockDeleteNonPayeEmploymentValidator
          .validate(deleteNonPayeEmploymentSavingsRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(deleteNonPayeEmploymentSavingsRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
