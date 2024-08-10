package com.nathan.reviewboard.http.controllers


import com.nathan.reviewboard.http.endpoints.ReviewEndpoints
import com.nathan.reviewboard.domain.data.{Company, UserID}
import com.nathan.reviewboard.services.{CompanyService, JWTService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

import collection.mutable

class ReviewController private (reviewService: ReviewService, jwtService: JWTService)
  extends BaseController
    with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
      .serverLogic(userID => req => reviewService.create(req, userID.id).either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic(id => reviewService.getById(id).either)

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic(companyId => reviewService.getByCompanyId(companyId).either)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getById, getByCompanyId)
}

object ReviewController {
  val makeZIO =
    for {
      reviewService <- ZIO.service[ReviewService]
      jwtService <- ZIO.service[JWTService]
    } yield new ReviewController(reviewService, jwtService)
}

