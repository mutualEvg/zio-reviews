package com.nathan.reviewboard.reposirories

import zio.*
import com.nathan.reviewboard.repositories.{CompanyRepository, CompanyRepositoryLive, Repository}
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

trait RepositorySpec {

  val initScript: String

  // test containers
  def createContainer() = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript(
        initScript
      ) // TODO store this under src/test/resources
    container.start()
    container
  }

  def closeContainer(container: PostgreSQLContainer[Nothing]) =
    container.stop()

  // create a DataSource to connect to the Postgres
  def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl())
    dataSource.setUser(container.getUsername())
    dataSource.setPassword(container.getPassword())
    dataSource
  }

  // use the DataSource (as a ZLayer) to build the Quill instance (as a ZLayer)
  val dataSourceLayer = ZLayer {
    for {
      container <- ZIO.acquireRelease(ZIO.attempt(createContainer()))(container =>
        ZIO.attempt(container.stop()).ignoreLogged
      )
      dataSource <- ZIO.attempt(createDataSource(container))
    } yield dataSource
  }
}
