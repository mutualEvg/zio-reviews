package com.nathan.reviewboard.reposirories

import zio.*
import zio.test.*
import com.nathan.reviewboard.syntax.*
import com.nathan.reviewboard.domain.data.{Company, CompanyFilter}
import com.nathan.reviewboard.repositories.{CompanyRepository, CompanyRepositoryLive, Repository}
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer

import java.sql.SQLException
import javax.sql.DataSource

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  override val initScript: String = "sql/companies.sql"
  private def genString() =
    scala.util.Random.alphanumeric.take(8).mkString
  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString(),
      location = Some(genString()),
      country = Some(genString()),
      industry = Some(genString()),
      tags = (1 to 3).map(_ => genString()).toList
    )


  val rtjvm = Company(1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
        } yield company

        program.assert {
          case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
          case _ => false
        }
      },
      test("creating a duplicate should error") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          _ <- repo.create(rtjvm)
          err <- repo.create(rtjvm).flip
        } yield err

        program.assert(_.isInstanceOf[SQLException])
      },
      test("get by id and slug") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          fetchedById <- repo.getById(company.id)
          fetchedBySlug <- repo.getBySlug(company.slug)
        } yield (company, fetchedById, fetchedBySlug)

        program.assert { case (company, fetchedById, fetchedBySlug) =>
          fetchedById.contains(company) && fetchedBySlug.contains(company)
        }
      },
      test("update record") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          updated <- repo.update(company.id, _.copy(url = "blog.rockthejvm.com"))
          fetchedById <- repo.getById(company.id)
        } yield (updated, fetchedById)

        program.assert { case (updated, fetchedById) =>
          fetchedById.contains(updated)
        }
      },
      test("delete record") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          _ <- repo.delete(company.id)
          fetchedById <- repo.getById(company.id)
        } yield fetchedById

        program.assert(_.isEmpty)
      },
      test("get all records") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          companies <- ZIO.collectAll((1 to 10).map(_ => repo.create(genCompany())))
          companiesFetched <- repo.get
        } yield (companies, companiesFetched)

        program.assert {
          case (companies, companiesFetched) =>
            companies.toSet == companiesFetched.toSet
        }
      },
      test("search by tag") {
        val program = for {
          repo     <- ZIO.service[CompanyRepository]
          company  <- repo.create(genCompany())
          fetched  <- repo.search(CompanyFilter(tags = company.tags.headOption.toList))
        } yield (fetched, company)

        program.assert { case (fetched, company) =>
          fetched.nonEmpty && fetched.tail.isEmpty && fetched.head == company
        }
      }



    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}

