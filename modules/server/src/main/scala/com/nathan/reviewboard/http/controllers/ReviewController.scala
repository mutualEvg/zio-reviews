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
    createEndpoint.serverLogicSuccess(req => reviewService.create(req, -1L /* TODO add user id */))

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogicSuccess(id => reviewService.getById(id))

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogicSuccess(companyId => reviewService.getByCompanyId(companyId))

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getById, getByCompanyId)
}

object ReviewController {
  val makeZIO = ZIO.service[ReviewService].map(reviewService => new ReviewController(reviewService))
}
