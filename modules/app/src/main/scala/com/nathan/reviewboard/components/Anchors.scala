package com.nathan.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import frontroute.*
import com.nathan.reviewboard.components.*

object Anchors {
  def renderNavLink(text: String, location: String, cssClass: String = "") =
    a(
      href := location,
      cls := cssClass,
      text
    )
}
