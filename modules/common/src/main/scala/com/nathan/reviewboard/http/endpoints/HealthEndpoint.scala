package com.nathan.reviewboard.http.endpoints

import sttp.tapir.*
import zio._

trait HealthEndpoint extends BaseEndpoint {

  val healthEndpoint = endpoint
    .tag("endpoint")
    .name("health")
    .description("helath check")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint = baseEndpoint
    .tag("endpoint")
    .name("error health")
    .description("health check - should fail")
    .get
    .in("health" / "error")
    .out(plainBody[String])
}
