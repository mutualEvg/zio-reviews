package com.nathan.reviewboard.repositories

import com.nathan.reviewboard.domain.data.{Company, CompanyFilter}
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def get: Task[List[Company]]
  def uniqueAttributes: Task[CompanyFilter]
  def search(filter: CompanyFilter): Task[List[Company]]
}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {
  import quill._

  inline given schema: SchemaMeta[Company]  = schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given upMeta: UpdateMeta[Company]  = updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(
          r => r
        )
    }
  override def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company].filter(_.id == lift(id))
    }.map(_.headOption)
  override def getBySlug(slug: String): Task[Option[Company]] =
    run {
      query[Company].filter(_.slug == lift(slug))
    }.map(_.headOption)
  override def get: Task[List[Company]] =
    run {
      query[Company]
    }
  override def update(id: Long, op: Company => Company): Task[Company] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Could not update: missing id $id"))
      updated <- run {
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(current)))
          .returning(
            r => r
          )
      }
    } yield updated
  override def delete(id: Long): Task[Company] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .delete
        .returning(
          r => r
        )
    }

  override def uniqueAttributes: Task[CompanyFilter] =
    for {
      locations  <- run(query[Company].map(_.location).distinct).map(_.flatMap(_.toList))
      countries  <- run(query[Company].map(_.country).distinct).map(_.flatMap(_.toList))
      industries <- run(query[Company].map(_.industry).distinct).map(_.flatMap(_.toList))
      tags       <- run(query[Company].map(_.tags)).map(_.flatten.toSet.toList)
    } yield CompanyFilter(locations, countries, industries, tags)

  /** select company from companies where location in filter.locations or country in ... or industry in ... or tags in
    * (select cl.tags from companies where cl.id == company.id)
    */

  override def search(filter: CompanyFilter): Task[List[Company]] =
    if (filter.isEmpty) get
    else
      run {
        query[Company]
          .filter {
            company =>
              liftQuery(filter.locations.toSet).contains(company.location) ||
              liftQuery(filter.countries.toSet).contains(company.country) ||
              liftQuery(filter.industries.toSet).contains(company.industry) ||
              sql"${company.tags} && ${lift(filter.tags)}".asCondition
            //liftQuery(filter.tags.toSet).filter(t => company.tags.contains(t)).nonEmpty
          }
      }
      /* override def search(filter: CompanyFilter): Task[List[Company]] =
    if (filter.isEmpty) get
    else
      run {
        query[Company]
          .filter {
            company =>
              liftQuery(filter.locations.toSet).contains(company.location) ||
              liftQuery(filter.countries.toSet).contains(company.country) ||
              liftQuery(filter.industries.toSet).contains(company.industry) ||
//              query[Company]
//                .filter(_.id == company.id)
//                .concatMap(_.tags) // List[String]
//                .filter(
//                  tag => liftQuery(filter.tags.toSet).contains(tag)
//                )
//                .nonEmpty
                liftQuery(filter.tags.toSet).filter(t => company.tags.contains(t)).nonEmpty

          }
      }*/

}

object CompanyRepositoryLive {
  val layer = ZLayer {
    // maybe speciffy SnakeCase.type
    ZIO
      .service[Quill.Postgres[SnakeCase]]
      .map(
        quill => CompanyRepositoryLive(quill)
      )
  }
}

object CompanyRepositoryDemo extends ZIOAppDefault {
  val program = for {
    repo <- ZIO.service[CompanyRepository]
    _    <- repo.create(Company(-1L, "rzk", "RZK", "rzk.com"))
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program.provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase),
      Quill.DataSource.fromPrefix("nath.db")
    )
}
