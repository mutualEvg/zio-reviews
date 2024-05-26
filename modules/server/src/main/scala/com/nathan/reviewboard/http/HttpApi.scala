package com.nathan.reviewboard.http

import com.nathan.reviewboard.http.controllers._


object HttpApi {

  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  def makeControllers = for {
    health <- HealthController.makeZIO
    companies <- CompanyController.makeZIO
    reviews <- ReviewController.makeZIO
    users <- UserController.makeZIO
    // add new controllers here
  } yield List(health, companies, reviews, users)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}

