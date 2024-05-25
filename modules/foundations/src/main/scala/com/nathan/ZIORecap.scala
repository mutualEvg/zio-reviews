package com.nathan

import zio.{Console, ZIO, ZIOAppDefault}

import scala.io.StdIn
//import scala.concurrent.duration._
import zio.*

object ZIORecap extends ZIOAppDefault {

  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("Something is wrong")
  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)
  val improveMofL = meaningOfLife.map(_ + 2)
  val printingMofL = improveMofL.flatMap(mol => ZIO.succeed(println(mol)))
  val smallProg = for {
    _ <- Console.printLine("What;s your name?")
    name <- ZIO.succeed(StdIn.readLine())
    _ <- Console.printLine(s"Welkome to ZIO $name")
  } yield ()
  val anAttempt = ZIO.attempt {
    println("Trying")
    val string: String = null
    println(string.length)
  }
  // catch errors effect-fully
  val catching = anAttempt.catchAll(e => ZIO.succeed(s"Returning some different value"))
  val catchingSelective = anAttempt.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Ignoring runtime exception $e")
    case _ => ZIO.succeed("Ignoring evrything else")
  }
  // Fibers
  val effectDelayedValue = ZIO.sleep(1.second) *> Random.nextIntBetween(0, 100)
  val aPair = for {
    a <- effectDelayedValue
    b <- effectDelayedValue
  } yield (a, b)
  val aPairPar = for {
    fibA <- effectDelayedValue.fork // scheduled to run in one fiber
    fibB <- effectDelayedValue.fork // scheduled to run in one fiber
    a <- fibA.join
    b <- fibB.join
  } yield (a, b) // this takes one second
  val interruptedFiber = for {
    fib <- effectDelayedValue.map(println).onInterrupt(ZIO.succeed(println("I'm interrupted"))).fork
    _ <- ZIO.sleep(500.millis) *> ZIO.succeed(println("Cancelling fiber")) *> fib.interrupt
    _ <- fib.join
  } yield ()

  val ignonredInterruption = for {
    fib <- ZIO.uninterruptible(effectDelayedValue.map(println).onInterrupt(ZIO.succeed(println("I'm interrupted")))).fork
    _ <- ZIO.sleep(500.millis) *> ZIO.succeed(println("Cancelling fiber")) *> fib.interrupt
    _ <- fib.join
  } yield ()

  // many API's
  val aPairPAr_v2 = effectDelayedValue.zipPar(effectDelayedValue)
  val randomX10 = ZIO.collectAllPar((1 to 10)
    .map(_ => effectDelayedValue))
    .forEachZIO(z => ZIO.succeed(println(z.mkString(", "))))
  // reduceAllPar, margeAllPar, foreachPar...

  // dependencies
  case class User(name: String, email: String)

  class UserSubscription(emailService: EmailService, userDatabase: UserDatabase) {
    //def subscribeUser(user: User): ZIO[Any, Throwable, Unit]
    //def subscribeUser(user: User): Task[Unit] = ZIO.succeed(s"subscribed $user") *> emailService.email(user)
    def subscribeUser(user: User): Task[Unit] = for {
      _ <- emailService.email(user)
      _ <- userDatabase.insert(user)
      _ <- ZIO.succeed(s"subscribed $user")
    } yield ()
  }

  class EmailService {
    def email(user: User): Task[Unit] = ZIO.succeed(s"Emailed $user")
  }

  class UserDatabase(connectionPool: ConnectionPool) {
    def insert(user: User): Task[Unit] = ZIO.succeed(s"inserted $user")
  }

  class ConnectionPool(nConnections: Int) {
    def get: Task[Connection] = ZIO.succeed(Connection())
  }

  case class Connection()

  def subscribe(user: User): ZIO[UserSubscription, Throwable, Unit] = for {
    sub <- ZIO.service[UserSubscription]
    _ <- sub.subscribeUser(user)
  } yield ()

  val program = for {
    _ <- subscribe(User("John", "john@azaza.com"))
    _ <- subscribe(User("Bon Jovi", "bon@azaza.com"))
  } yield ()

  object UserSubscription {
    val live: ZLayer[EmailService with UserDatabase, Nothing, UserSubscription] =
      ZLayer.fromFunction((emailS, userD) => new UserSubscription(emailS, userD))
  }

  object EmailService {
    val live: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(new EmailService)
  }

  object UserDatabase {
    val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(new UserDatabase(_))
  }

  object ConnectionPool {
    def live(nConnections: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(ConnectionPool(nConnections))
  }

 


  //override def run: IO[IOException, Unit] = Console.printLine("Hi ZIO")
  //override def run = interruptedFiber
  //override def run = //program.provide(UserSubscription(EmailService(), UserDatabase()))
  override def run = program.provide(
    ConnectionPool.live(10), // build me a ConnectionPool
    UserDatabase.live, // build me a UserDatabase, using the ConnectionPool
    EmailService.live, // build me a EmailService
    UserSubscription.live // build me a UserSubscription
  )
}