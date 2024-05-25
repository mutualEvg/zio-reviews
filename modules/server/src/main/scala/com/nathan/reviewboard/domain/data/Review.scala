package com.nathan.reviewboard.domain.data

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec
import java.time.Instant

final case class Review(
    id: Long, // PK
    companyId: Long,
    userId: Long, // FK
    // scores
    management: Int, // 1-5
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
)

object Review {
  given codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}
