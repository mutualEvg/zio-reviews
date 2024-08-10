package com.nathan.reviewboard.http.controllers

import com.nathan.reviewboard.http.endpoints.CompanyEndpoints
import com.nathan.reviewboard.domain.data.{Company, UserID}
import com.nathan.reviewboard.services.{CompanyService, JWTService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

import collection.mutable

class CompanyController private (service: CompanyService, jwtService: JWTService) extends BaseController with CompanyEndpoints {
  // TODO implementations

  // in-memory "database"
  /*val db = mutable.Map[Long, Company](
    //-1L -> Company(-1L, "Wrong company", "No company", "nothing.com")
  )*/

  // create >> should have specified type or type inference brakes
//  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogicSuccess { req =>
//    ZIO.succeed {
//      val newId = db.keys.maxOption.getOrElse(0L) + 1
//      val newCompany = req.toCompany(newId)
//      db += (newId -> newCompany)
//      newCompany
//    }
//  }
//
//  val getAll: ServerEndpoint[Any, Task] =
//    getAllEndpoint.serverLogicSuccess(_ => ZIO.succeed(db.values.toList))
//
//  val getById: ServerEndpoint[Any, Task] =
//    getByIdEndpoint.serverLogicSuccess { id =>
//      ZIO
//        .attempt(id.toLong)
//        .flatMap(/* TODO get by id*/)
//        .catchSome {
//          case _: NumberFormatException =>
//          // TODO get by slug
//        }
//    }

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { userID => req =>
      service.create(req).either
    }

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => service.getAll.either)

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      ZIO
        .attempt(id.toLong)
        .flatMap(service.getById)
        .catchSome {
          case _: NumberFormatException =>
          service.getBySlug(id)
        }.either
    }


  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)
}
object CompanyController {
  //val makeZIO = ZIO.succeed(new CompanyController)
  val makeZIO = for {
    service <- ZIO.service[CompanyService]
    jwtService <- ZIO.service[JWTService]
  } yield new CompanyController(service, jwtService)
}
