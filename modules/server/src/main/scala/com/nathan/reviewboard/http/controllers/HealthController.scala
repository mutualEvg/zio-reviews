package com.nathan.reviewboard.http.controllers

import com.nathan.reviewboard.domain.errors.HttpError
import com.nathan.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.{plainBody, statusCode}
import sttp.tapir.server.ServerEndpoint
import zio.*

import scala.language.postfixOps

class HealthController extends BaseController with HealthEndpoint {

  val health: ServerEndpoint[Any, Task] = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  val error = errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Boom!")).either) // Task[Either[Throwable, String]]
  override val routes: List[ServerEndpoint[Any, Task]] = List(health, error)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}