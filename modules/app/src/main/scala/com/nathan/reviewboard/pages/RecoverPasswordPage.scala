package com.nathan.reviewboard.pages

import com.nathan.reviewboard.common._
import org.scalajs.dom.html.Element
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.{given, *}
import org.scalajs.dom
import zio.*
import com.nathan.reviewboard.core.ZJS.*
import com.nathan.reviewboard.core.*
import com.nathan.reviewboard.http.requests._
import com.nathan.reviewboard.components.*

case class RecoverPasswordState(
    email: String = "",
    token: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState {
  def errorList: List[Option[String]] =
    List(
      Option.when(!email.matches(Constants.emailRegex))("Email is invalid"),
      Option.when(token.isEmpty)("Token can't be empty"),
      Option.when(newPassword.isEmpty)("Password can't be empty"),
      Option.when(newPassword != confirmPassword)("Passwords must match")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
}

object RecoverPasswordPage extends FormPage[RecoverPasswordState]("Reset Password") {

  //override val stateVar: Var[RecoverPasswordState] = Var(RecoverPasswordState())
  override def basicState: RecoverPasswordState = RecoverPasswordState()

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    renderInput(
      "Your Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, e) => s.copy(email = e, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Recovery Token (from email)",
      "token-input",
      "text",
      true,
      "The token",
      (s, t) => s.copy(token = t, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "New Password",
      "password-input",
      "password",
      true,
      "Your new password",
      (s, p) => s.copy(newPassword = p, showStatus = false, upstreamStatus = None)
    ),
    renderInput(
      "Confirm Password",
      "confirm-password-input",
      "password",
      true,
      "Confirm password",
      (s, p) => s.copy(confirmPassword = p, showStatus = false, upstreamStatus = None)
    ),
    button(
      `type` := "button",
      "Reset Password",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    ),
    Anchors.renderNavLink(
      "Need a password recovery token?",
      "/forgot",
      "auth-link"
    )

  )

  val submitter = Observer[RecoverPasswordState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    } else {
      useBackend(_.user.recoverPasswordEndpoint(RecoverPasswordRequest(state.email, state.token, state.newPassword)))
        .map { _ =>
          stateVar.update(
            _.copy(
              showStatus = true,
              upstreamStatus = Some(Right("Success! You can log in now."))
            )
          )
        }
        .tapError { e =>
          ZIO.succeed {
            stateVar.update(_.copy(showStatus = true, upstreamStatus = Some(Left(e.getMessage))))
          }
        }
        .runJs
    }
  }

}

