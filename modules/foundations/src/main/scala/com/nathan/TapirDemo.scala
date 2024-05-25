package com.nathan

import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.{Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint

import scala.collection.mutable

object TapirDemo extends ZIOAppDefault {


  val simplestEndpoint = endpoint
    .tag("endpoint")
    .name("simple")
    .description("simplest endpoint possible")
    .get
    .in("simple")
    .out(plainBody[String])
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  val simpleServerProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default
    ).toHttp(simplestEndpoint)
  )

  // simulate a job board
  val db: mutable.Map[Long, Job] = mutable.Map(
    1L -> Job(1L, "Instructor", "rockthejvm.com", "Rock the JVM")
  )

  // create
  val createEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("create")
    .description("Create a job")
    .in("jobs")
    .post
    .in(jsonBody[CreateJobRequest])
    .out(jsonBody[Job])
    .serverLogicSuccess(req => ZIO.succeed {
      // insert a new job in my "db"
      val newId = db.keys.max + 1
      val newJob = Job(newId, req.title, req.url, req.company)
      db += (newId -> newJob)
      newJob
    })

  // get by id
  val getByIdEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("getById")
    .description("Get job by id")
    .in("jobs" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Job]])
    .serverLogicSuccess(id => ZIO.succeed(db.get(id)))

  // get all
  val getAllEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("getAll")
    .description("Get all jobs")
    .in("jobs")
    .get
    .out(jsonBody[List[Job]])
    .serverLogicSuccess(_ => ZIO.succeed(db.values.toList))

  val serverProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default
    ).toHttp(List(createEndpoint, getByIdEndpoint, getAllEndpoint))
  )


  //  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = simpleServerProgram.provide(
  //    Server.default //0.0.0.0:8080
  //  )
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = serverProgram.provide(
    Server.default //0.0.0.0:8080
  )
}

