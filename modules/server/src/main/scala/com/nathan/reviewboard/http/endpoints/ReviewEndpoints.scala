package com.nathan.reviewboard.http.endpoints

import com.nathan.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import sttp.tapir.json.zio._
import sttp.tapir.generic.auto._
import com.nathan.reviewboard.domain.data.Review

trait ReviewEndpoints extends BaseEndpoint {

  val createEndpoint = baseEndpoint
    .tag("Reviews")
    .name("create")
    .description("Add a review for a company")
    .in("reviews")
    .post
    .in(jsonBody[CreateReviewRequest])
    .out(jsonBody[Review])

  val getByIdEndpoint = baseEndpoint
    .tag("Reviews")
    .name("getById")
    .description("Get a review by its id")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint = baseEndpoint
    .tag("Reviews")
    .name("getByCompanyId")
    .description("Get reviews for a company")
    .in("reviews" / "company" / path[Long]("id"))
    .get
    .out(jsonBody[List[Review]])

}
