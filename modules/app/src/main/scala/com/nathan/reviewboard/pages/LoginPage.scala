package com.nathan.reviewboard.pages

import com.nathan.reviewboard.common.Constants.companyLogoPlaceholder
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import frontroute.*
import org.scalajs.dom
import com.nathan.reviewboard.common.Constants.*

object LoginPage {

  def apply() = 
//    div("login page")
    div(
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
            img(
              cls := "home-logo",
              src := logoImage,
              alt := "Rock the JVM"
            )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          div(cls := "top-section", h1(span("Log In"))),
          div(
            cls := "page-status-errors",
            "This is an error"
          ),
          div(
            cls := "page-status-success",
            "This is a success"
          ),
          form(
            nameAttr := "signin",
            cls := "form",
            idAttr := "form",
            // an input of type text
            div(
              cls := "row",
              div(
                cls := "col-md-12",
                div(
                  cls := "form-input",
                  label(
                    forId := "form-id-1-todo",
                    cls := "form-label",
                    span("*"),
                    "Email"
                  ),
                  input(
                    `type` := "text",
                    cls := "form-control",
                    idAttr := "form-id-1-todo",
                    placeholder := "Your email"
                  )
                )
              )
            ),
            // an input of type password
            div(
              cls := "row",
              div(
                cls := "col-md-12",
                div(
                  cls := "form-input",
                  label(
                    forId := "form-id-2-todo",
                    cls := "form-label",
                    span("*"),
                    "Password"
                  ),
                  input(
                    `type` := "password",
                    cls := "form-control",
                    idAttr := "form-id-2-todo",
                    placeholder := "Your password"
                  )
                )
              )
            ),
            button(
              `type` := "button",
              "Log In"
            )
          )
        )
      )
    )
}
