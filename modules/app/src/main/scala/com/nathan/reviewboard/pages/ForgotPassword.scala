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

case class ForgotPasswordState(
    email: String = "",
    upstreamStatus: Option[Either[String, String]] = None,
    override val showStatus: Boolean = false
) extends FormState {
  override val errorList: List[Option[String]] =
    List(
      Option.when(!email.matches(Constants.emailRegex))("Email is invalid")
    ) ++ upstreamStatus.map(_.left.toOption).toList

  def maybeSuccess: Option[String] =
    upstreamStatus.flatMap(_.toOption)
}

object ForgotPasswordPage extends FormPage[ForgotPasswordState]("Forgot password") {
  override val stateVar: Var[ForgotPasswordState]                   = Var(ForgotPasswordState())
  override def renderChildren() = List(
    renderInput(
      "Email",
      "email-input",
      "text",
      true,
      "Your email",
      (s, e) => s.copy(email = e, showStatus = false, upstreamStatus = None)
    ),
    button(
      `type` := "button",
      "Recover Password",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )

  val submitter = Observer[ForgotPasswordState] { state =>
    if (state.hasErrors) {
      stateVar.update(_.copy(showStatus = true))
    } else {
      useBackend(_.user.forgotPasswordEndpoint(ForgotPasswordRequest(state.email)))
        .map { _ =>
          stateVar.update(
            _.copy(
              showStatus = true,
              upstreamStatus = Some(Right("Check your email!"))
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
