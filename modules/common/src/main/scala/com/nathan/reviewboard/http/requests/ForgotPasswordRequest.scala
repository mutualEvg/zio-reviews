package com.nathan.reviewboard.http.requests

import zio.json.JsonCodec

case class ForgotPasswordRequest(email: String) derives JsonCodec
