package com.nathan.reviewboard.http.responses

import zio.json.JsonCodec

case class UserResponse (email: String) derives JsonCodec
