package com.nathan.reviewboard.http.controllers

import com.nathan.reviewboard.domain.data.{Company, CompanyFilter, User, UserID, UserToken}
import com.nathan.reviewboard.http.requests.CreateCompanyRequest
import com.nathan.reviewboard.services.{CompanyService, JWTService}
import zio.*
import zio.test.*
import sttp.client3.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import zio.json
import zio.json.*
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import com.nathan.reviewboard.syntax.*

object CompanyControllerSpec extends ZIOSpecDefault {

  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private val rzk = Company(1, "rzk", "RZK", "rzk.com")
  private val serviceStub = new CompanyService {
    override def create(req: CreateCompanyRequest): Task[Company] = ZIO.succeed(rzk)

    override def getAll: Task[List[Company]] = ZIO.succeed(List(rzk))

    override def getById(id: Long): Task[Option[Company]] = ZIO.succeed(
      if (id == 1L) Some(rzk)
      else None
    )

    override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed(
      if (slug == rzk.slug) Some(rzk)
      else None
    )

    override def allFilters: Task[CompanyFilter] = ZIO.succeed(CompanyFilter())

    override def search(filter: CompanyFilter): Task[List[Company]] = ZIO.succeed(List(rzk))
  }
  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) =
    for {
      controller <- CompanyController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointFun(controller))
          .backend()
      )
    } yield backendStub

  private val jwtServiceStub = new JWTService {
    override def createToken(user: User): Task[UserToken] =
      ZIO.succeed(UserToken(user.email, "ALL_IS_GOOD", 999999999L))

    override def verifyToken(token: String): Task[UserID] =
      ZIO.succeed(UserID(1, "ezra@haskell.com"))
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post company") {
        val program = for {
          // create the controller
//          controller <- CompanyController.makeZIO
//          // build tapir backend
//          backendStub <- ZIO.succeed(
//            TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
//              .whenServerEndpointRunLogic(controller.create)
//              .backend()
//          )
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("RZK", "rzk.com").toJson)
            .header("Authorization", "Bearer azaza mocked shit")
            .send(backendStub)
        } yield response.body
        // run http request
        // inspect http response
        // inspect http response
        program.assert{ respBody  =>
            respBody
              .toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(Company(1, "rzk", "RZK", "rzk.com"))
          }
      },
      test("get all") {
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
            .get(uri"/companies")
            .send(backendStub)
        } yield response.body
        // controller
        // stub server
        // run request
        // inspect response
        // TODO make it assert extension
        assertZIO(program)(
          Assertion.assertion("test assertion") { respBody =>
            respBody.asInstanceOf[Either[String, String]].toOption
              .flatMap(_.fromJson[List[Company]].toOption)
              .contains(List(rzk))
          }
        )
      },
      test("get by id") {
        val program = for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/companies/1")
            .send(backendStub)
        } yield response.body
        // controller
        // stub server
        // run request
        // inspect response
        assertZIO(program)(
          Assertion.assertion("test assertion") { respBody =>
            respBody.asInstanceOf[Either[String, String]].toOption
              .flatMap(_.fromJson[Company].toOption)
              .contains(rzk)
          }
        )
      },
    ).provide(
      ZLayer.succeed(serviceStub),
      ZLayer.succeed(jwtServiceStub)
    )
}



//import zio.*
//import zio.test.*
//
//object CompanyControllerSpec extends ZIOSpecDefault {
//  override def spec: Spec[TestEnvironment & Scope, Any] =
//    suite("CompanyControllerSpec")(
//      test("simple test") {
//        // code under test
//        assertZIO(ZIO.succeed(1 + 1))(
//          Assertion.assertion("basic math")(_ == 2)
//        )
//      }
//    )
//}
