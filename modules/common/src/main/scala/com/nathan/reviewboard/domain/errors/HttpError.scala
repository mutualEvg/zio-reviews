package com.nathan.reviewboard.domain.errors

import sttp.model.StatusCode

final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
) extends RuntimeException(message, cause)

object HttpError {
  def decode(tuple: (StatusCode, String)) =
    HttpError(tuple._1, tuple._2, new RuntimeException(tuple._2))

  def encode(error: Throwable) = error match {
    case UnauthorizedException(massage) => (StatusCode.Unauthorized, massage)
    // TODO add more cases
    case _ => (StatusCode.InternalServerError, error.getMessage)
  }

}
