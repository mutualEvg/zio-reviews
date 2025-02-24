package com.nathan.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.nathan.reviewboard.pages.*

object Router {
  def apply() =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          // potential children
          (pathEnd | path("companies")) { // localhost:1234 or localhost:1234/ or localhost:1234/companies
            CompaniesPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignUpPage()
          },
          path("profile") {
            ProfilePage()
          },
          path("logout") {
            LogoutPage()
          },
          noneMatched {
            NotFoundPage()
          }
        ),
      )
    )

  //  def apply() =
//    mainTag(
//      routes(
//        div(
//          cls := "container-fluid",
//          // potential children
//          pathEnd { // localhost:1234 or localhost:1234/
//            div("main page")
//          },
//          path("companies") { // localhost:1234/companies
//            div("companies page")
//          }
//        )
//      )
//    )
}

