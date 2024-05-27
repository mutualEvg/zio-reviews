package com.nathan.reviewboard.services

import com.nathan.reviewboard.config.{Configs, EmailServiceConfig}
import zio.*

import java.util.Properties
import javax.mail.internet.MimeMessage
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}

trait EmailService {
  def sendEmail(to: String, subject: String, content: String): Task[Unit]
  def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit] = {
    val subject = "The JVM: Password Recovery"
    val content = s"""
    <div style="
      border: 1px solid black;
      padding: 20px;
      font-family: sans-serif;
      line-height: 2;
      font-size: 20px;
    ">
      <h1>The JVM: Password Recovery</h1>
      <p>Your password recovery token is: <strong>$token</strong></p>
      <p>🥰 from the JVM</p>
    </div>
  """
    sendEmail(to, subject, content)
  }

}

class EmailServiceLive private (config: EmailServiceConfig) extends EmailService {

  val host: String = config.host
  val port: Int    = config.port
  val user: String = config.user
  val pass: String = config.pass

  println(s"Host: $host")
  println(s"Port: $port")
  println(s"User: $user")
  println(s"Pass: $pass")

  override def sendEmail(to: String, subject: String, content: String): Task[Unit] = {
    val messageZIO = for {
      prop    <- propsResource
      session <- createSession(prop)
      message <- createMessage(session)("ezra@haskell.com", to, subject, content)
    } yield message

    messageZIO.map(
      message => Transport.send(message)
    )
  }

  private val propsResource: Task[Properties] = {
    val prop = new Properties
    prop.put("mail.smtp.auth", true)
    prop.put("mail.smtp.starttls.enable", "true")
    prop.put("mail.smtp.host", host)
    prop.put("mail.smtp.port", port)
    prop.put("mail.smtp.ssl.trust", host)
    ZIO.succeed(prop)
  }

  private def createSession(prop: Properties): Task[Session] =
    ZIO.attempt(
      Session.getInstance(
        prop,
        new Authenticator {
          override def getPasswordAuthentication: PasswordAuthentication =
            new PasswordAuthentication(user, pass)
        }
      )
    )

  private def createMessage(
      session: Session
  )(from: String, to: String, subject: String, content: String): Task[MimeMessage] = {
    val message = new MimeMessage(session)
    message.setFrom(from)
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(content, "text/html; charset=utf-8")
    ZIO.succeed(message)
  }

}
object EmailServiceLive {
  val layer = ZLayer {
    ZIO
      .service[EmailServiceConfig]
      .map(conf => new EmailServiceLive(conf))
  }

  val configuredLayer =
    Configs.makeConfigLayer[EmailServiceConfig]("nath.email") >>> layer
}

object EmailServiceDemo extends ZIOAppDefault {
  val program = for {
    emailService <- ZIO.service[EmailService]
    //_            <- emailService.sendEmail("evg.losev11@gmail.com", "Hi from the JVM", "This email service works.")
    _            <- emailService.sendPasswordRecoveryEmail(
      "evg.losev11@gmail.com", "FUCKINGTOKEN123")
    _            <- Console.printLine("Email done.")
  } yield ()

  override def run = program.provide(EmailServiceLive.configuredLayer)
}
