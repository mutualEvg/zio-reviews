package com.nathan.reviewboard.services

import com.nathan.reviewboard.domain.data.{Company, CompanyFilter}

import scala.collection.mutable
import zio.*

import collection.mutable
import com.nathan.reviewboard.http.requests.CreateCompanyRequest
import com.nathan.reviewboard.repositories.CompanyRepository

// BUSINESS LOGIC
// in between the HTTP layer and the DB layer
trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def allFilters: Task[CompanyFilter]
  def search(filter: CompanyFilter): Task[List[Company]]
}
// FIXME can be removed if required
//object CompanyService {
//  val dummyLayer = ZLayer.succeed(new CompanyServiceDummy)
//}

class CompanyServiceLive private(repo: CompanyRepository) extends CompanyService {
  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll: Task[List[Company]] =
    repo.get

  override def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repo.getBySlug(slug)

  override def allFilters: Task[CompanyFilter] = repo.uniqueAttributes

  override def search(filter: CompanyFilter): Task[List[Company]] =
    repo.search(filter)
}
object CompanyServiceLive {
  val layer = ZLayer {
    for {
      repo <- ZIO.service[CompanyRepository]
    } yield new CompanyServiceLive(repo)
  }
}


//class CompanyServiceDummy extends CompanyService {
//  val db = mutable.Map[Long, Company]()
//
//  override def create(req: CreateCompanyRequest): Task[Company] =
//    ZIO.succeed {
//      val newId = db.keys.maxOption.getOrElse(0L) + 1
//      val newCompany = req.toCompany(newId)
//      db += (newId -> newCompany)
//      newCompany
//    }
//  override def getAll: Task[List[Company]] =
//    ZIO.succeed(db.values.toList)
//
//  override def getById(id: Long): Task[Option[Company]] =
//    ZIO.succeed(db.get(id))
//
//  override def getBySlug(slug: String): Task[Option[Company]] =
//    ZIO.succeed(db.values.find(_.slug == slug))
//}

