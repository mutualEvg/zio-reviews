package com.nathan.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import frontroute.*
import org.scalajs.dom

object NotFoundPage {

  def apply() =
    div("404 Invalid page")
}
