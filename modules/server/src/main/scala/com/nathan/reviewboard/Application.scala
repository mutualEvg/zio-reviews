package com.nathan.reviewboard

import com.nathan.reviewboard.config.{Configs, JWTConfig}
import com.nathan.reviewboard.http.HttpApi
import com.nathan.reviewboard.http.controllers.{CompanyController, HealthController}
import com.nathan.reviewboard.repositories.{CompanyRepositoryLive, RecoveryTokensRepositoryLive, ReviewRepositoryLive, UserRepositoryLive}
import com.nathan.reviewboard.repositories.Repository.dataLayer
import com.nathan.reviewboard.services.{CompanyService, CompanyServiceLive, EmailServiceLive, JWTServiceLive, ReviewServiceLive, UserServiceLive}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.endpoint
import sttp.tapir.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server

object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default.appendInterceptor(
          CORSInterceptor.default
        )
      ).toHttp(endpoints)
    )
    _ <- Console.printLine("Rock the JVM!")
  } yield ()


  //override def run = serverProgram.provide(Server.default, CompanyService.dummyLayer)
  override def run = serverProgram.provide(
    Server.default,
    //Configs.makeConfigLayer[JWTConfig]("nath.jwt"),
    // services
    CompanyServiceLive.layer,
    ReviewServiceLive.layer,
    UserServiceLive.layer,
    JWTServiceLive.configuredLayer,
    EmailServiceLive.configuredLayer,
    //repos
    CompanyRepositoryLive.layer,
    ReviewRepositoryLive.layer,
    UserRepositoryLive.layer,
    RecoveryTokensRepositoryLive.configuredLayer,
    // other dependencies
    dataLayer
  )

}
