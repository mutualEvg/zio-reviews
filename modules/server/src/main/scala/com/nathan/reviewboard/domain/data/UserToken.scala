package com.nathan.reviewboard.domain.data

final case class UserToken(
    email: String,
    token: String,
    expires: Long
)

final case class UserID(
    id: Long,
    email: String
)
