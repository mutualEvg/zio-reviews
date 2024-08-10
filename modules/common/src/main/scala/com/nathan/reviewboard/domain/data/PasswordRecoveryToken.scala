package com.nathan.reviewboard.domain.data

case class PasswordRecoveryToken(
    email: String,
    token: String,
    expiration: Long
)
