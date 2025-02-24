package com.nathan.reviewboard.core

import com.nathan.reviewboard.common.Constants.companyLogoPlaceholder
import com.raquo.laminar.api.L.{given, *}
import com.raquo.laminar.codecs.*
import frontroute.*
import org.scalajs.dom
import com.nathan.reviewboard.common.Constants.*
import com.nathan.reviewboard.core.ZJS.*
import com.nathan.reviewboard.http.requests.LoginRequest
import zio.ZIO
import com.nathan.reviewboard.common.Constants.companyLogoPlaceholder
import com.nathan.reviewboard.components.*
import com.nathan.reviewboard.domain.data.*
import com.raquo.laminar.api.L.{*, given}
import sttp.client3.*
import org.scalajs.dom
import scala.scalajs.js.*
import com.nathan.reviewboard.core.*
/*
TODO: var d = new Date()
  d.getTime()
 */
object Session {
  val stateName: String = "userState"
  val userState: Var[Option[UserToken]] = Var(Option.empty)
  
  def isActive(): Boolean = {
    loadUserState()
    userState.now().nonEmpty
  }
  
  def setUserState(token: UserToken): Unit = {
    Storage.set(stateName, token)
  }
  def loadUserState(): Unit = {
    // clears any expired token
    Storage
      .get[UserToken](stateName)
      .filter(_.expires * 1000 <= new Date().getTime())
      .foreach(_ => Storage.remove(stateName))
    // retrieves the user token (known to be valid)
    userState.set(
      Storage.get[UserToken](stateName)
    )
  }

  def clearUserState(): Unit = {
    Storage.remove(stateName)
    userState.set(Option.empty)
  }

  def getUserState: Option[UserToken] = {
    loadUserState()
    userState.now()
  }

}
