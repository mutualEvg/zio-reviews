package com.nathan.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import org.scalajs.dom.html.Element
import com.raquo.laminar.nodes.ReactiveHtmlElement

import com.nathan.reviewboard.core.*

case class LogoutPageState() extends FormState {
  override def errorList: List[Option[String]] = List()
  override def maybeSuccess: Option[String] = None
  override def showStatus: Boolean = false
}

object LogoutPage extends FormPage[LogoutPageState]("Log Out") {
  //override val stateVar: Var[LogoutPageState] = Var(LogoutPageState())
  override def basicState: LogoutPageState = LogoutPageState()

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    div(
      onMountCallback(_ => Session.clearUserState()),
      cls := "centered-text",
      "You have been successfully logged out."
    )
  )
}
