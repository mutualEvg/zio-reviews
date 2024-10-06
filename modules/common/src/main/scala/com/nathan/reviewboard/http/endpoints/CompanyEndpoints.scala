package com.nathan.reviewboard.http.endpoints

import com.nathan.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.nathan.reviewboard.domain.data.{Company, CompanyFilter}

trait CompanyEndpoints extends BaseEndpoint {
  val createEndpoint =
    secureBaseEndpoint
      .tag("companies")
      .name("create")
      .description("create a listing for a company")
      .in("companies")
      .post
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])

  val getAllEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getAll")
      .description("get all company listings")
      .in("companies")
      .get
      .out(jsonBody[List[Company]])

  val getByIdEndpoint =
    baseEndpoint
      .tag("companies")
      .name("getById")
      .description("get company by its id (or maybe by something else)")
      .in("companies" / path[String]("id"))
      .get
      .out(jsonBody[Option[Company]])

  val allFiltersEndpoint =
    baseEndpoint
      .tag("Companies")
      .name("allFilters")
      .description("Get all possible search filters")
      .in("companies" / "filters")
      .get
      .out(jsonBody[CompanyFilter])

}
