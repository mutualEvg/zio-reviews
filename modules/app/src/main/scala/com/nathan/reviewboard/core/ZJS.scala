package com.nathan.reviewboard.core

import com.nathan.reviewboard.core.BackendClientLive.configuredLayer
import com.raquo.laminar.api.L.{*, given}
import sttp.tapir.Endpoint
import zio.*

import scala.annotation.targetName

object ZJS {

//  def backendCall[A](clientFun: BackendClient => Task[A]): ZIO[BackendClient, Throwable, A] =
//    ZIO.serviceWithZIO[BackendClient](clientFun)

//  def backendCall = ZIO.serviceWithZIO[BackendClient]
  def useBackend = ZIO.serviceWithZIO[BackendClient]

  extension [E <: Throwable, A](zio: ZIO[BackendClient, E, A])
    def emitTo(eventBus: EventBus[A]) =
      Unsafe.unsafe {
        implicit unsafe =>
          Runtime.default.unsafe.fork(
            zio.tap(
              value => ZIO.attempt(eventBus.emit(value))
            ).provide(configuredLayer))
      }
    def toEventStream: EventStream[A] = {
      val bus = EventBus[A]()
      emitTo(bus)
      bus.events
    }

    def runJs =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(zio.provide(BackendClientLive.configuredLayer))
        //Runtime.default.unsafe.runToFuture(zio.provide(BackendClientLive.configuredLayer))
      }

  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    def apply(payload: I): Task[O] =
      ZIO.service[BackendClient]
        .flatMap { _.endpointRequestZIO(endpoint)(payload) }
        .provide(configuredLayer)

  extension [I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])
    @targetName("applySecure")
    def apply(payload: I): Task[O] =
      ZIO
        .service[BackendClient]
        .flatMap(_.secureEndpointRequestZIO(endpoint)(payload))
        .provide(configuredLayer)

}
