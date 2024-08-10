package com.nathan.reviewboard.http.endpoints

import sttp.tapir.endpoint
import sttp.tapir.*
import com.nathan.reviewboard.domain.errors.HttpError
import com.nathan.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.{plainBody, statusCode}
import sttp.tapir.server.ServerEndpoint
import zio.*

trait BaseEndpoint {

  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String]) // (StatusCode, String)
    /*(StatusCode, String) => MyHttpError*/
    /* MyHttpError => (StatusCode, String)*/
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

  val secureBaseEndpoint =
    baseEndpoint
      .securityIn(auth.bearer[String]())
}
