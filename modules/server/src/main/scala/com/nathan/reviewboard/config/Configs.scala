package com.nathan.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.{Tag, ZIO, ZLayer}
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfig

object Configs {

  // Load the entire config
  val rootConfig = ConfigFactory.load()

  // Adjust the makeConfigLayer to take a path and use the correct sub-config
  def makeConfigLayer[C](path: String)(using desc: Descriptor[C], tag: Tag[C]): ZLayer[Any, Throwable, C] =
    TypesafeConfig.fromTypesafeConfig(
      ZIO.attempt(rootConfig.getConfig(path)),
      descriptor[C]
    )
}
