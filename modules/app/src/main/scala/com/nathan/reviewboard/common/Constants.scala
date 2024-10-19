package com.nathan.reviewboard.common

import scala.scalajs.js.annotation.*
import scala.scalajs.js

object Constants {

  // email regex
  val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

  
  @js.native
  @JSImport("/static/img/star2.png", JSImport.Default)
  val logoImage: String = js.native
  
  @js.native
  @JSImport("/static/img/star3.png", JSImport.Default)
  val logoImageBig: String = js.native

  @js.native
  @JSImport("/static/img/star2.png", JSImport.Default)
  val companyLogoPlaceholder: String = js.native
  
}
