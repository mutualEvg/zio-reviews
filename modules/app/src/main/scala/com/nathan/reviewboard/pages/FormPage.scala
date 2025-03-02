package com.nathan.reviewboard.pages

import com.nathan.reviewboard.common.Constants.*
import com.nathan.reviewboard.components.*
import com.nathan.reviewboard.core.*
import com.nathan.reviewboard.core.ZJS.*
import com.nathan.reviewboard.domain.data.*
import com.nathan.reviewboard.http.requests.LoginRequest
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.*
import org.scalajs.dom
import sttp.client3.*
import zio.ZIO


trait FormState {
  def errorList: List[Option[String]]
  def showStatus: Boolean
  def maybeSuccess: Option[String]

  def maybeError = errorList.find(_.isDefined).flatten
  def hasErrors  = errorList.exists(_.isDefined)
  def maybeStatus: Option[Either[String, String]] =
    maybeError.map(Left(_)).orElse(maybeSuccess.map(Right(_))).filter(_ => showStatus)
}


abstract class FormPage[S <: FormState](title: String) {

  //val stateVar: Var[S]
  def renderChildren(): List[ReactiveHtmlElement[dom.html.Element]]

  def basicState: S

  val stateVar: Var[S] = Var(basicState)
  
  //  val submitter = Observer[FormState] {
//    state =>
//      //println(s"State = $state")
//      dom.console.log(s"Current state is: $state")
//      //useBackend(_.company.getAllEndpoint(())).runJs
//      // check state errors – if so, show errors in the panel
//      // if no errors, trigger the backend call
//      // if backend gave us an error, show that
//      // if success, set the user token, navigate away
//      if (state.hasErrors) {
//        stateVar.update(_.copy(showStatus = true))
//      } else {
//        dom.console.log(s"Current state is: $state, trigger backend call")
////        useBackend(_.company.getAllEndpoint(())).runJs
//        useBackend(_.user.loginEndpoint(LoginRequest(state.email, state.password)))
//          .map {
//            userToken =>
//              Session.setUserState(userToken)
//              println(s"userToken = $userToken")
//              stateVar.set(FormState())
//              BrowserNavigation.replaceState("/")
//          }
//          .tapError {
//            e =>
//              ZIO.succeed { stateVar.update(_.copy(showStatus = true, upstreamError = Some(e.getMessage))) }
//          }
//          .runJs
//      }
//  }

  def apply() = { //div("login page")
    div(
      onUnmountCallback(_ => stateVar.set(basicState)),
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
            //cls := "home-logo",
            src := logoImageBig,
            alt := "Star"
          )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          div(cls := "top-section", h1(span(title))),
          children <-- stateVar.signal
            .map(_.maybeStatus)
            .map(renderStatus)
            .map(_.toList),
//          renderChildren(),
          form(
            nameAttr := "signin",
            cls      := "form",
            idAttr   := "form",
            renderChildren()
          )
        )
      )
    )
  }
//  def renderChildren() = List(
//    // an input of type text
//    renderInput(
//      "Email",
//      "email-input",
//      "text",
//      true,
//      "Your email",
//      (s, e) => s.copy(email = e, showStatus = false, upstreamError = None)
//    ),
//    renderInput(
//      "Password",
//      "password-input",
//      "password",
//      true,
//      "Your password",
//      (s, p) => s.copy(password = p, showStatus = false, upstreamError = None)
//    ),
//    // an input of type password
//    button(
//      `type` := "button",
//      "Log In",
//      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
//    )
//  )

  def renderStatus(status: Option[Either[String, String]]) = status.map {
    case Left(error) =>
      div(
        cls := "page-status-errors",
        error
      )

    case Right(message) =>
      div(
        cls := "page-status-success",
        //child.text <-- stateVar.signal.map(_.toString)
        message
      )
  }


  //  def renderError(error: String) =
//    div(
//      cls := "page-status-errors",
//      error
//    )
//  def maybeRenderSuccess(shouldShow: Boolean = false) =
//    if (shouldShow)
//      div(
//        cls := "page-status-success",
//        //"This is a success",
//        child.text <-- stateVar.signal.map(_.toString)
//      )
//    else div()

  def renderInput(
      name: String,
      uid: String = "form-id-1-todo",
      kind: String,
      required: Boolean = false,
      plcHolder: String = "empty",
      updateFn: (S, String) => S
  ) = {
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls   := "form-label",
            if (required) span("*") else span(),
            name
          ),
          input(
            `type`      := kind,
            cls         := "form-control",
            idAttr      := uid,
            placeholder := plcHolder,
            onInput.mapToValue --> stateVar.updater(updateFn)
          )
        )
      )
    )
  }
}
