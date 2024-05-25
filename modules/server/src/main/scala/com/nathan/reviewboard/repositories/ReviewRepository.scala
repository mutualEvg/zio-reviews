package com.nathan.reviewboard.repositories

import zio.*
import com.nathan.reviewboard.domain.data.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]
}

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {
  import quill.*

  inline given reviewSchema: SchemaMeta[Review]     = schemaMeta[Review]("reviews")
  inline given reviewInsertMeta: InsertMeta[Review] = insertMeta[Review](_.id, _.created, _.updated)
  inline given reviewUpdateMeta: UpdateMeta[Review] = updateMeta[Review](_.id, _.companyId, _.userId, _.created)

  def create(review: Review): Task[Review] =
    run(
      query[Review]
        .insertValue(lift(review))
        .returning(
          r => r
        )
    )

  def getById(id: Long): Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id)).take(1)).map(_.headOption)

  def getByCompanyId(companyId: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(companyId)))

  def getByUserId(userId: Long): Task[List[Review]] =
    run(query[Review].filter(_.userId == lift(userId)))

  def update(id: Long, op: Review => Review): Task[Review] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"update review failed: missing id $id"))
    updatedReview = op(current)
    updated <- run(query[Review].filter(_.id == lift(id))
      .update(
        /*review.companyId == updated.companyId &&
            review.userId == updated.userId &&
            review.management == updated.management &&
            review.culture == updated.culture &&
            review.salary == updated.salary &&
            review.benefits == updated.benefits &&
            review.wouldRecommend == updated.wouldRecommend &&
            updated.review == "not too bad" &&
            review.created == updated.created &&
            review.updated != updated.updated*/
        r => r.companyId -> lift(updatedReview.companyId),
        r => r.userId -> lift(updatedReview.userId),
        r => r.management -> lift(updatedReview.management),
        r => r.culture -> lift(updatedReview.culture),
        r => r.salary -> lift(updatedReview.salary),
        r => r.benefits -> lift(updatedReview.benefits),
        r => r.wouldRecommend -> lift(updatedReview.wouldRecommend),
        r => r.review -> lift(updatedReview.review),
        r => r.created -> lift(updatedReview.created),
        r => r.updated -> lift(updatedReview.updated),
    ).returning(r => r))
  } yield updated

  def delete(id: Long): Task[Review] =
    run(query[Review].filter(_.id == lift(id)).delete.returning(r => r))

}

object ReviewRepositoryLive {
  val layer = ZLayer {
    ZIO
      .service[Quill.Postgres[SnakeCase.type]]
      .map(
        quill => ReviewRepositoryLive(quill)
      )
  }
}
