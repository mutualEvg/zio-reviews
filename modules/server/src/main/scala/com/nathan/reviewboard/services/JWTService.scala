package com.nathan.reviewboard.services

import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.{JWT, JWTVerifier}
import com.auth0.jwt.algorithms.Algorithm
import com.nathan.reviewboard.domain.data.{User, UserID, UserToken}
import zio.*

import java.time.Instant

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}

class JWTServiceLive (clock: java.time.Clock) extends JWTService {
  private val SECRET = "secret" // TODO pass this from config
  private val TTL = 30 * 24 * 3600 // TODO pass this from config
  private val ISSUER = "ezra.com"
  private val CLAIM_USERNAME = "username"

  private val algorithm = Algorithm.HMAC512(SECRET)

  private val verifier: JWTVerifier =
    JWT
      .require(algorithm)
      .withIssuer(ISSUER)
      .asInstanceOf[BaseVerification]
      .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
    now <- ZIO.attempt(clock.instant())
    expiration <- ZIO.succeed(now.plusSeconds(TTL))
    token <- ZIO.attempt(
      JWT
        .create()
        .withIssuer(ISSUER)
        .withIssuedAt(now)
        .withExpiresAt(expiration)
        .withSubject(user.id.toString)
        .withClaim(CLAIM_USERNAME, user.email)
        .sign(algorithm)
    )
  } yield UserToken(user.email, token, expiration.getEpochSecond)

  override def verifyToken(token: String): Task[UserID] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userId <- ZIO.attempt(
        UserID(
          decoded.getSubject.toLong,
          decoded.getClaim(CLAIM_USERNAME).asString()
        )
      )
    } yield userId

}

object JWTServiceLive {
  val layer = ZLayer {
    Clock.javaClock.map(clock => new JWTServiceLive(clock))
  }
}

object JWTServiceDemo extends ZIOAppDefault {
  val program = for {
    service <- ZIO.service[JWTService]
    token <- service.createToken(User(1L, "ezra@haskell.com", "unimportant"))
    _ <- Console.printLine(token)
    userId <- service.verifyToken(token.token)
    _ <- Console.printLine(userId.toString)
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program.provide(
      JWTServiceLive.layer
    )
}


//object JWTServiceDemo {
//  def main(args: Array[String]) = {
//    val algorithm = Algorithm.HMAC512("secret")
//    val jwt = JWT
//      .create()
//      .withIssuer("ezra.com")
//      .withIssuedAt(Instant.now())
//      .withExpiresAt(Instant.now().plusSeconds(30 * 24 * 3600))
//      .withSubject("1") // user identifier
//      .withClaim("username", "ezra@haskell.com")
//      .sign(algorithm)
//    println(jwt)
//
//    // verification
//    val verifier: JWTVerifier =
//      JWT
//        .require(algorithm)
//        .withIssuer("ezra.com")
//        .asInstanceOf[BaseVerification]
//        .build(java.time.Clock.systemDefaultZone())
//
//    val decoded = verifier.verify(jwt)
//    val userId = decoded.getSubject()
//    val userEmail = decoded.getClaim("username").asString()
//    println(userId)
//    println(userEmail)
//
//  }
//  //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9 // header algortithm etc
//  //  claims \/\/\/
//  // .eyJzdWIiOiIxIiwiaXNzIjoicm9ja3RoZWp2bS5jb20iLCJleHAiOjE3MTkyNjg4NzksImlhdCI6MTcxNjY3Njg3OSwidXNlcm5hbWUiOiJkYW5pZWxAcm9ja3RoZWp2bS5jb20ifQ
//  //  salt and stuff...\/\/\/
//  // .jXYbk0lLS_ANbz75cysvRIBNn4vq23GPRlnO7PWTQRKCOU7NCvADoPtUq_qYyVpxlC3Go821077ai3eoaziFtA
//}
