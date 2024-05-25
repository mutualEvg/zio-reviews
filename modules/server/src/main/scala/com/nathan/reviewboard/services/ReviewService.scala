package com.nathan.reviewboard.services

import com.nathan.reviewboard.domain.data.Review
import com.nathan.reviewboard.http.requests.CreateReviewRequest
import com.nathan.reviewboard.repositories.ReviewRepository
import zio.*

import java.time.Instant

trait ReviewService {
  def create(request: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService {

  override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
    repo.create(
      Review(
        id = -1L,  // Typically a placeholder for databases that auto-generate IDs
        companyId = request.companyId,
        management = request.management,
        culture = request.culture,
        salary = request.salary,
        benefits = request.benefits,
        wouldRecommend = request.wouldRecommend,
        review = request.review,
        userId = userId,
        created = Instant.now(),
        updated = Instant.now()
      )
    )


  override def getById(id: Long): Task[Option[Review]] =
    repo.getById(id)

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repo.getByCompanyId(companyId)

  override def getByUserId(userId: Long): Task[List[Review]] =
    repo.getByUserId(userId)

}
object ReviewServiceLive {
  val layer = ZLayer {
    for {
      repo <- ZIO.service[ReviewRepository]
    } yield new ReviewServiceLive(repo)
  }
}