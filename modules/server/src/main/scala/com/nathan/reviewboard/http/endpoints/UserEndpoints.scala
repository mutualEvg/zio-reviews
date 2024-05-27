package com.nathan.reviewboard.http.endpoints

import com.nathan.reviewboard.domain.data.UserToken
import com.nathan.reviewboard.http.requests.{DeleteAccountRequest, ForgotPasswordRequest, LoginRequest, RecoverPasswordRequest, RegisterUserAccount, UpdatePasswordRequest}
import com.nathan.reviewboard.http.responses.UserResponse
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

trait UserEndpoints extends BaseEndpoint {

  // POST /users { email, password } -> { email }
  val createUserEndpoint =
    baseEndpoint
      .tag("Users")
      .name("register")
      .description("Register a user account with username and password")
      .in("users")
      .post
      .in(jsonBody[RegisterUserAccount]) // email: String, password: String
      .out(jsonBody[UserResponse])

  // PUT /users/password { email, oldPassword, newPassword } -> { email }
  // TODO - should ba an authorized endpoint (JWT)
  val updatePasswordEndpoint =
    secureBaseEndpoint
      .tag("Users")
      .name("update password")
      .description("Update user password")
      .in("users" / "password")
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[UserResponse])

  // DELETE /users { email, password } -> { email }
  // TODO - should ba an authorized endpoint (JWT)
  val deleteEndpoint =
    secureBaseEndpoint
      .tag("Users")
      .name("delete")
      .description("Delete user account")
      .in("users")
      .delete
      .in(jsonBody[DeleteAccountRequest])
      .out(jsonBody[UserResponse])

  // POST /users/login { email, password } -> { email, accessToken, expiration }
  val loginEndpoint =
    baseEndpoint
      .tag("Users")
      .name("login")
      .description("Log in and generate a JWT token")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginRequest])
      .out(jsonBody[UserToken])

  // forgot password flow
  // POST /users/forgot { email } - 200 OK
  val forgotPasswordEndpoint =
    baseEndpoint
      .tag("Users")
      .name("forgot password")
      .description("Trigger email for password recovery")
      .in("users" / "forgot")
      .post
      .in(jsonBody[ForgotPasswordRequest])

  // recover password
  // POST /users/recover { email, token, newPassword }
  val recoverPasswordEndpoint =
    baseEndpoint
      .tag("Users")
      .name("recover password")
      .description("Set new password based on OTP")
      .in("users" / "recover")
      .post
      .in(jsonBody[RecoverPasswordRequest])
  
}

