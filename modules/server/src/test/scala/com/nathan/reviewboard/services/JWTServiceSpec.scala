package com.nathan.reviewboard.services

import com.nathan.reviewboard.config.JWTConfig
import com.nathan.reviewboard.domain.data.User
import zio.*
import zio.test.*

object JWTServiceSpec extends ZIOSpecDefault {
  
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("JWTServiceSpec")(
      test("create and validate token") {
        for {
          service <- ZIO.service[JWTService]
          userToken <- service.createToken(
            User(1L, "ezra@haskell.com", "unimportant")
          )
          // string
          userId <- service.verifyToken(userToken.token)
        } yield assertTrue(
          userId.id == 1L &&
            userId.email == "ezra@haskell.com"
          )

      }
    ).provide(
      JWTServiceLive.layer,
      ZLayer.succeed(JWTConfig("secret", 3600))
    )
}

