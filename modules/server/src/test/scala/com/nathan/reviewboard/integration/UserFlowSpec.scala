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
import com.nathan.reviewboard.http.requests.{DeleteAccountRequest, LoginRequest, RegisterUserAccount, UpdatePasswordRequest}
import com.nathan.reviewboard.http.responses.UserResponse
import com.nathan.reviewboard.reposirories.RepositorySpec
import com.nathan.reviewboard.repositories.{Repository, UserRepository, UserRepositoryLive}
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
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

  extension [A: JsonCodec](backend: SttpBackend[Task, Nothing]) {
    def sendRequest[B: JsonCodec](
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
    def post[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, None)
    def postAuth[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, Some(token))

    def put[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, None)
    def putAuth[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, Some(token))

    def delete[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.DELETE, path, payload, None)
    def deleteAuth[B: JsonCodec](path: String, payload: A, token: String): Task[Option[B]] =
      sendRequest(Method.DELETE, path, payload, Some(token))
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserFlowSpec")(
      test("create user") {
        for {
          backendStub   <- backendStubZIO
          maybeResponse <- backendStub.post[UserResponse]("/users", RegisterUserAccount("ezra@haskell.com", "ezra"))
        } yield assertTrue(maybeResponse.contains(UserResponse("ezra@haskell.com")))
      },
      test("create and log in") {
        for {
          backendStub   <- backendStubZIO
          maybeResponse <- backendStub.post[UserResponse]("/users", RegisterUserAccount("ezra@haskell.com", "ezra"))
          maybeToken    <- backendStub.post[UserToken]("/users/login", LoginRequest("ezra@haskell.com", "ezra"))
          /*maybeResponse <- basicRequest
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
            )*/
        } yield assertTrue(
          maybeToken.exists(_.email == "ezra@haskell.com")
        )

      },
      test("change password") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub.post[UserResponse](
            "/users",
            RegisterUserAccount("ezra@haskell.com", "ezra")
          )
          userToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest("ezra@haskell.com", "ezra"))
            .someOrFail(new RuntimeException("Authentication failed"))
          _ <- backendStub
            .putAuth[UserResponse](
              "/users/password",
              UpdatePasswordRequest("ezra@haskell.com", "ezra", "scalarulez"),
              userToken.token
            )
          maybeOldToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest("ezra@haskell.com", "ezra"))
          maybeNewToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest("ezra@haskell.com", "scalarulez"))
        } yield assertTrue(
          maybeOldToken.isEmpty && maybeNewToken.nonEmpty
        )

      },
      test("delete account") {
        for {
          backendStub <- backendStubZIO
          userRepo    <- ZIO.service[UserRepository]
          maybeResponse <- backendStub.post[UserResponse](
            "/users",
            RegisterUserAccount("ezra@haskell.com", "ezra")
          )
          maybeOldUser <- userRepo.getByEmail("ezra@haskell.com")
          userToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest("ezra@haskell.com", "ezra"))
            .someOrFail(new RuntimeException("Authentication failed"))
          _ <- backendStub
            .deleteAuth[UserResponse](
              "/users",
              DeleteAccountRequest("ezra@haskell.com", "ezra"),
              userToken.token
            )
          maybeUser <- userRepo.getByEmail("ezra@haskell.com")
        } yield assertTrue(
          maybeOldUser.exists(_.email == "ezra@haskell.com") && maybeUser.isEmpty
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
