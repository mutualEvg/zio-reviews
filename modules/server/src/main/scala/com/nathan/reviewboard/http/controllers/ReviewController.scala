package com.nathan.reviewboard.http.controllers


import com.nathan.reviewboard.http.endpoints.ReviewEndpoints
import com.nathan.reviewboard.domain.data.Company
import com.nathan.reviewboard.services.{CompanyService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

import collection.mutable

class ReviewController private (reviewService: ReviewService)
  extends BaseController
    with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic(req => reviewService.create(req, -1L /* TODO add user id */).either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic(id => reviewService.getById(id).either)

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic(companyId => reviewService.getByCompanyId(companyId).either)

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getById, getByCompanyId)
}

object ReviewController {
  val makeZIO = ZIO.service[ReviewService].map(reviewService => new ReviewController(reviewService))
}
