package com.nathan.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.{Tag, ZIO, ZLayer}
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfig

object Configs {

  val plainConfig = ConfigFactory.load().getConfig("nath.jwt")

  def makeConfigLayer[C](path: String)(using desc: Descriptor[C], tag: Tag[C]): ZLayer[Any, Throwable, C] =
    TypesafeConfig.fromTypesafeConfig(
      ZIO.attempt(plainConfig),
      descriptor[C]
    )

}
