package com.nathan.reviewboard.domain.data

import zio.json.JsonCodec

final case class UserToken(
    email: String,
    token: String,
    expires: Long
) derives JsonCodec

final case class UserID(
    id: Long,
    email: String
) derives JsonCodec
