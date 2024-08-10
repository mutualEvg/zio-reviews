package com.nathan.reviewboard.domain.data

final case class User(
    id: Long,
    email: String,
    hashedPassword: String
) {
  def toUserID = UserID(id, email)
}
