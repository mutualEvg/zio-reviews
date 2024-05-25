package com.nathan

import zio.ZIOAppDefault
import zio._
import io.getquill._
import io.getquill.jdbczio.Quill

object QuillDemo extends ZIOAppDefault {

  val program = for {
    repo <- ZIO.service[JobRepository]
    _ <- repo.create(Job(-1, "Software engineer", "rockthejvm.com", "Rock the JVM"))
    _ <- repo.create(Job(-1, "Instructor", "rockthejvm.com", "Rock the JVM"))
  } yield ()

  override def run = program.provide(
    JobRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("mydbconf")// reads conf in application.conf and spins app the datasource
  )
}

// repository
trait JobRepository {
  def create(job: Job): Task[Job]
  def update(id: Long, op: Job => Job): Task[Job]
  def delete(id: Long): Task[Job]
  def getById(id: Long): Task[Option[Job]]
  def get: Task[List[Job]]
}
class JobRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends JobRepository {
  // step 1

  import quill.* // some methods e.g. run a query

  // step 2 - schemas for create, update ...
  inline given schema: SchemaMeta[Job]  = schemaMeta[Job]("jobs") // specify the table name
  inline given insMeta: InsertMeta[Job] = insertMeta[Job](_.id)   // columns to be excluded in insert statements
  inline given updMeta: UpdateMeta[Job] = updateMeta[Job](_.id)   // same for update statements

  def create(job: Job): Task[Job] =
    run {
      query[Job]
        .insertValue(lift(job))
        .returning(
          j => j
        )
    }

  def update(id: Long, op: Job => Job): Task[Job] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Could not update: missing key $id"))
    updated <- run {
      query[Job]
        .filter(_.id == lift(id))
        .updateValue(lift(op(current)))
        .returning(
          j => j
        )
    }
  } yield updated

  def delete(id: Long): Task[Job] =
    run {
      query[Job]
        .filter(_.id == lift(id))
        .delete
        .returning(
          j => j
        )
    }

  def getById(id: Long): Task[Option[Job]] =
    run {
      query[Job]
        .filter(_.id == lift(id)) // select * form jobs where id == ?
    }.map(_.headOption)

  def get: Task[List[Job]] = run(query[Job])
}

object JobRepositoryLive {
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase]].map(quill => JobRepositoryLive(quill))
  }
}
