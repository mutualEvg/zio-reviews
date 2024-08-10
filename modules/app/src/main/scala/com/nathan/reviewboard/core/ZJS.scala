package com.nathan.reviewboard.core

import com.nathan.reviewboard.common.Constants.companyLogoPlaceholder
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import frontroute.*
import com.nathan.reviewboard.components.*
import com.nathan.reviewboard.config.BackendClientConfig
import com.nathan.reviewboard.domain.data.*
import com.nathan.reviewboard.http.endpoints.CompanyEndpoints
import com.nathan.reviewboard.domain.data.*
import com.nathan.reviewboard.http.endpoints.CompanyEndpoints
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.model.Uri
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*

object ZJS {

  val backend                            = FetchZioBackend()
  val interpreter: SttpClientInterpreter = SttpClientInterpreter()
  val backendClientLive =
    new BackendClientLive(backend, interpreter, config = BackendClientConfig(Some(uri"http://localhost:8080")))

  def backendCall[A](clientFun: BackendClient => Task[A]): Task[A] =
    clientFun(backendClientLive)

  extension [E <: Throwable, A](zio: ZIO[Any, E, A])
    def emitTo(eventBus: EventBus[A]) =
      Unsafe.unsafe {
        implicit unsafe =>
          Runtime.default.unsafe.fork(
            zio.tap(
              value => ZIO.attempt(eventBus.emit(value))
            )
          )
      }
  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    def apply(payload: I): Task[O] = backendClientLive.endpointRequestZIO(endpoint)(payload)

}
