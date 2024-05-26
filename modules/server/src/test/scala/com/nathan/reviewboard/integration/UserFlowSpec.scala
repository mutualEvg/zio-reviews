package com.nathan.reviewboard.integration

import com.nathan.reviewboard.config.JWTConfig
import com.nathan.reviewboard.domain.data.UserToken
import zio.*
import zio.test.*
import sttp.client3.*
import zio.json.*
import sttp.tapir.generic.auto.*
import com.nathan.reviewboard.services.*
import com.nathan.reviewboard.http.controllers.*
import com.nathan.reviewboard.http.requests.{LoginRequest, RegisterUserAccount}
import com.nathan.reviewboard.http.responses.UserResponse
import com.nathan.reviewboard.reposirories.RepositorySpec
import com.nathan.reviewboard.repositories.{Repository, UserRepositoryLive}
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import sttp.monad.syntax.MonadErrorOps

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec {

  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  override val initScript: String = "sql/integration.sql"

  // http controller
  // service
  // repository
  // test container

  private def backendStubZIO =
    for {
      controller <- UserController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointsRunLogic(controller.routes)
          .backend()
      )
    } yield backendStub

  extension (backend: SttpBackend[Task, Nothing]) {
    def sendRequest[A: JsonCodec, B: JsonCodec](
        method: Method,
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth
        .bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body)
        .map(
          _.toOption.flatMap(
            payload => payload.fromJson[B].toOption
          )
        )
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserFlowSpec")(
      test("create user") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- basicRequest
            .post(uri"/users")
            .body(
              RegisterUserAccount(
                "ezra@haskell.com",
                "ezra"
              ).toJson
            )
            .send(backendStub)
            .map(_.body)
            .map(
              _.toOption.flatMap(
                payload => payload.fromJson[UserResponse].toOption
              )
            )
        } yield assertTrue(maybeResponse.contains(UserResponse("ezra@haskell.com")))
      },
      test("create and log in") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- basicRequest
            .post(uri"/users")
            .body(
              RegisterUserAccount("ezra@haskell.com", "ezra").toJson
            )
            .send(backendStub)
            .map(_.body)
            .map(
              _.toOption.flatMap(
                payload => payload.fromJson[UserResponse].toOption
              )
            )
          maybeToken <- basicRequest
            .post(uri"/users/login")
            .body(LoginRequest("ezra@haskell.com", "ezra").toJson)
            .send(backendStub)
            .map(_.body)
            .map(
              _.toOption.flatMap(
                payload => payload.fromJson[UserToken].toOption
              )
            )
        } yield assertTrue(
          maybeToken.exists(_.email == "ezra@haskell.com")
        )

      }
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      UserRepositoryLive.layer,
      Repository.quillLayer,
      dataSourceLayer,
      ZLayer.succeed(JWTConfig("secret", 3600)),
      Scope.default
    )

}
