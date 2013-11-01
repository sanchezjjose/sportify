package models

import javax.mail._
import javax.mail.internet._
import javax.mail.PasswordAuthentication
import java.util.Properties

class MailSender {

  private val SMTP_HOST_NAME = "smtp.sendgrid.net"
  private val SMTP_AUTH_USER = "***REMOVED***"
  private val SMTP_AUTH_PWD  = "***REMOVED***"

  def nextGameReminder() {

    Game.findNextGame.map { game =>

      val players = game.playersIn.map { id =>
        "- " + User.findById(id).get.firstName
      }

      val html = ("You have an upcoming game against '%s' on %s <br><br> " +
                   "The address is %s, New York, New York <br> " +
                    "<u>Note</u>: <i> %s </i> <br><br> " +
                     "So far, we have %s players in: <br> %s <br> " +
                      "<br><b>Remember to bring your game shirts, and lets get this W!</b><br>")
                       .format(game.opponent, game.startTime, game.address.replace(".", ""), game.locationDetails, players.size, players.mkString("<br>"))

      val props = new Properties()
      props.put("mail.transport.protocol", "smtp")
      props.put("mail.smtp.host", SMTP_HOST_NAME)
      props.put("mail.smtp.port", "587")
      props.put("mail.smtp.auth", "true")

      val auth = new SMTPAuthenticator()
      val mailSession = Session.getInstance(props, auth)

      // uncomment for debugging infos to stdout
      // mailSession.setDebug(true);

      val transport = mailSession.getTransport()
      val message = new MimeMessage(mailSession)
      val multipart = new MimeMultipart("alternative")

      val part = new MimeBodyPart()
      part.setContent(html, "text/html")

      multipart.addBodyPart(part)

      message.setContent(multipart)
      message.setFrom(new InternetAddress("sportify@email.heroku.com"))
      message.setSubject("You have a basketball game on " + game.startTime)
      message.addRecipient(Message.RecipientType.TO, new InternetAddress("***REMOVED***"))

      transport.connect()
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
      transport.close()
    }
  }

  private class SMTPAuthenticator extends javax.mail.Authenticator {
    override def getPasswordAuthentication(): PasswordAuthentication = {
      val username = SMTP_AUTH_USER
      val password = SMTP_AUTH_PWD

      new PasswordAuthentication(username, password)
    }
  }
}
