package com.nathan.reviewboard.repositories

import com.nathan.reviewboard.config.{Configs, RecoveryTokensConfig}
import com.nathan.reviewboard.domain.data.PasswordRecoveryToken
import io.getquill.jdbczio.Quill
import io.getquill.{InsertMeta, SchemaMeta}
import zio.*
import com.nathan.reviewboard.domain.data.Company
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait RecoveryTokensRepository {
  def getToken(email: String): Task[Option[String]]
  def checkToken(email: String, token: String): Task[Boolean]
}
class RecoveryTokensRepositoryLive private (
    recoveryToken: RecoveryTokensConfig,
    quill: Quill.Postgres[SnakeCase],
    userRepo: UserRepository
) extends RecoveryTokensRepository {

  import quill._
  inline given schemaMeta1: SchemaMeta[PasswordRecoveryToken] = schemaMeta[PasswordRecoveryToken]("recovery_tokens")
  inline given insMeta: InsertMeta[PasswordRecoveryToken]     = insertMeta[PasswordRecoveryToken]()
  inline given upMeta: UpdateMeta[PasswordRecoveryToken]      = updateMeta[PasswordRecoveryToken](_.email)

  private val tokenDuration = 600 // TODO pass this from config

  private def randomUppercaseString(len: Int): Task[String] =
    ZIO.succeed(scala.util.Random.alphanumeric.take(len).mkString.toUpperCase)

  private def findToken(email: String): Task[Option[String]] =
    run(query[PasswordRecoveryToken].filter(_.email == lift(email))).map(_.headOption.map(_.token))

  private def replaceToken(email: String): Task[String] =
    for {
      token <- randomUppercaseString(8)
      _ <- run(
        query[PasswordRecoveryToken]
          .updateValue(
            lift(PasswordRecoveryToken(email, token, java.lang.System.currentTimeMillis() + tokenDuration))
          )
          .returning(
            r => r
          )
      )
    } yield token

  private def generateToken(email: String): Task[String] =
    for {
      token <- randomUppercaseString(8)
      _ <- run(
        query[PasswordRecoveryToken]
          .insertValue(
            lift(
              PasswordRecoveryToken(
                email,
                token,
                java.lang.System.currentTimeMillis() + tokenDuration
              )
            )
          )
          .returning(
            r => r
          )
      )
    } yield token

  private def makeFreshToken(email: String): Task[String] =
    findToken(email).flatMap {
      case Some(_) => replaceToken(email)
      case _       => generateToken(email)
    }
  // find token in the table
  // if so, replace
  // if not, create

  override def getToken(email: String): Task[Option[String]] =
    userRepo.getByEmail(email).flatMap {
      case None    => ZIO.none
      case Some(_) => makeFreshToken(email).map(Some(_))
    }

  override def checkToken(email: String, token: String): Task[Boolean] =
    run(
      query[PasswordRecoveryToken].filter(
        r => r.email == lift(email) && r.token == lift(token)
      )
    ).map(_.nonEmpty)

}

object RecoveryTokensRepositoryLive {
  val layer = ZLayer {
    for {
      config <- ZIO.service[RecoveryTokensConfig]
      quill    <- ZIO.service[Quill.Postgres[SnakeCase.type]]
      userRepo <- ZIO.service[UserRepository]
    } yield new RecoveryTokensRepositoryLive(config, quill, userRepo)
  }

  val configuredLayer =
    Configs.makeConfigLayer[RecoveryTokensConfig]("nath.recoveryTokens") >>> layer
}
