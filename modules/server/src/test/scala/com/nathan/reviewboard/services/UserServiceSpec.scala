package com.nathan.reviewboard.services

import com.nathan.reviewboard.domain.data.{User, UserID, UserToken}
import com.nathan.reviewboard.repositories.{RecoveryTokensRepository, UserRepository}
import zio.*
import zio.test.*

object UserServiceSpec extends ZIOSpecDefault {

  println(UserServiceLive.Hasher.generateHash("ezra"))
  println(UserServiceLive.Hasher.validateHash("ezra",
    "1000:D56F98C6193A3CB77E4549CE2CF3C996FCDB69831FC59F64:652ECB3401A283A8F4D5C21389480DCE99D19AA3C89EA380"))
  val hashedPassword =
    "1000:D56F98C6193A3CB77E4549CE2CF3C996FCDB69831FC59F64:652ECB3401A283A8F4D5C21389480DCE99D19AA3C89EA380"

  val ezra = User(
    1L,
    "ezra@haskell.com",
    hashedPassword
  )

  val stubRepoLayer = ZLayer.succeed {
    new UserRepository {
      val db = collection.mutable.Map[Long, User](1L -> ezra)

      override def create(user: User): Task[User] = ZIO.succeed {
        db += (user.id -> user)
        user
      }

      override def getById(id: Long): Task[Option[User]] =
        ZIO.succeed(db.get(id))

      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(db.values.find(_.email == email))

      override def update(id: Long, op: User => User): Task[User] = ZIO.attempt {
        val newUser = op(db(id))
        db += (newUser.id -> newUser)
        newUser
      }

      override def delete(id: Long): Task[User] = ZIO.attempt {
        val user = db(id)
        db -= id
        user
      }

    }
  }

  val stubEmailsLayer = ZLayer.succeed {
    new EmailService {
      override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ZIO.unit
    }
  }

  val stubTokenRepoLayer = ZLayer.succeed {
    new RecoveryTokensRepository {
      val db = collection.mutable.Map[String, String]()

      override def checkToken(email: String, token: String): Task[Boolean] =
        ZIO.succeed(db.get(email).contains(token))

      override def getToken(email: String): Task[Option[String]] = ZIO.attempt {
        val token = util.Random.alphanumeric.take(8).mkString.toUpperCase()
        db += (email -> token)
        Some(token)
      }
    }
  }

  val stubJwtLayer = ZLayer.succeed {
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user.email, "BIG ACCESS", Long.MaxValue))

      override def verifyToken(token: String): Task[UserID] =
        ZIO.succeed(UserID(ezra.id, ezra.email))
    }
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserServiceSpec")(
      test("create and validate a user") {
        for {
          service <- ZIO.service[UserService]
          user <- service.registerUser(ezra.email, "ezra")
          valid <- service.verifyPassword(ezra.email, "ezra")
        } yield assertTrue(valid && user.email == ezra.email)
      },
      test("validate correct credentials") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword(ezra.email, "ezra")
        } yield assertTrue(valid)
      },
      test("invalidate incorrect credentials") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword(ezra.email, "somethingelse")
        } yield assertTrue(!valid)
      },
      test("invalidate non-existent user") {
        for {
          service <- ZIO.service[UserService]
          valid <- service.verifyPassword("someone@gmail.com", "somethingelse")
        } yield assertTrue(!valid)
      },
      test("update password") {
        for {
          service <- ZIO.service[UserService]
          newUser <- service.updatePassword(ezra.email, "ezra", "scalarulez")
          oldValid <- service.verifyPassword(ezra.email, "ezra")
          newValid <- service.verifyPassword(ezra.email, "scalarulez")
        } yield assertTrue(newValid && !oldValid)
      },
      test("delete non-existent user should fail") {
        for {
          service <- ZIO.service[UserService]
          err <- service.deleteUser("someone@gmail.com", "something").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete with incorrect credentials fail") {
        for {
          service <- ZIO.service[UserService]
          err <- service.deleteUser(ezra.email, "something").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete user") {
        for {
          service <- ZIO.service[UserService]
          user <- service.deleteUser(ezra.email, "ezra")
        } yield assertTrue(user.email == ezra.email)
      }

    ).provide(
      UserServiceLive.layer,
      stubJwtLayer,
      stubRepoLayer,
      stubEmailsLayer,
      stubTokenRepoLayer
    )

}

