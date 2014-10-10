package models

import java.util.Properties
import java.util.concurrent.{Executors, TimeUnit}
import javax.mail.internet._
import javax.mail.{PasswordAuthentication, _}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers._
import models.CustomPlaySalatContext._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Handles scheduling of the emails.
 */
class MailScheduler extends Loggable with Helper {

  private val emailScheduler = Executors.newScheduledThreadPool(2)

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def startChecking() {
    emailScheduler.scheduleAtFixedRate(new Runnable {
      def run() {
        log.info("checking for next game to create email messages...")

        Team.getNextGameByTeam.foreach { case (team, nextGame) =>
          team.player_ids.foreach { playerId =>
            User.findByPlayerId(playerId).foreach { user =>
              val sendAt = format.parseDateTime(nextGame.start_time).minusHours(20).getMillis

              // This is pretty bad. Look into how to use unique index on game id and recipient instead.
              if (!EmailMessage.findByGameIdAndRecipient(nextGame._id, user.email).isDefined) {
                val newMessage = EmailMessage(generateRandomId(), user._id, nextGame._id, sendAt, None, 0, user.email)
                EmailMessage.insert(newMessage)
              }
            }
          }
        }
      }
    }, 0, 2, TimeUnit.HOURS)
  }

  def startSending() {
    emailScheduler.scheduleAtFixedRate(new Runnable {
      def run() {
        log.info("checking for new emails to send...")

        Team.getNextGameByTeam.foreach { case (team, nextGame) =>
          EmailMessage.findUnsent(nextGame._id).foreach { message =>
            new MailSender().sendNextGameReminderEmail(message, team, nextGame)
          }
        }
      }
    }, 0, 1, TimeUnit.HOURS)
  }

  /**
   * TODO: create subscription model to represent emailable users.
   */
  private def shouldEmail(user: User): Boolean = {
    user.email.trim != "" &&
    user.email.toLowerCase != "irosa8621@yahoo.com"
  }
}

/**
 * Handles sending of the emails.
 */
class MailSender extends Loggable with Config {

  private val SMTP_HOST_NAME = "smtp.sendgrid.net";
  private val SMTP_AUTH_USER = System.getenv("SENDGRID_USERNAME");
  private val SMTP_AUTH_PWD  = System.getenv("SENDGRID_PASSWORD");

  def sendNextGameReminderEmail(emailMessage: EmailMessage, team: Team, game: Game) {

    try {
      val props = new Properties()
      props.put("mail.transport.protocol", "smtp")
      props.put("mail.smtp.host", SMTP_HOST_NAME)
      props.put("mail.smtp.port", "587")
      props.put("mail.smtp.auth", "true")

      val auth = new SMTPAuthenticator()
      val mailSession = Session.getInstance(props, auth)

      val transport = mailSession.getTransport
      val message = new MimeMessage(mailSession)
      val multipart = new MimeMultipart("alternative")

      val userId = emailMessage.user_id
      val recipient = emailMessage.recipient

      val shouldSendEmail = {
        (Config.environment == Environment.DEVELOPMENT && recipient == "***REMOVED***") ||
         Config.environment == Environment.PRODUCTION
      }

      log.info("Should send email to %s? %s".format(recipient, shouldSendEmail))

      // Only send emails to me if in development
      if (shouldSendEmail) {

        val playersIn = game.players_in.map { playerId =>
          "- " + User.findByPlayerId(playerId).get.first_name
        }

        val html = views.html.email.reminderEmail(team, game, userId, playersIn).toString()

        val part = new MimeBodyPart()
        part.setContent(html, "text/html")

        multipart.addBodyPart(part)

        message.setContent(multipart)
        message.setFrom(new InternetAddress("sportify@email.heroku.com"))
        message.setSubject("You have an upcoming game on " + game.start_time)
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient))

        log.info("Sending an email to " + recipient + " for game id " + game._id)

        transport.connect()
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
        transport.close()

        EmailMessage.markAsSent(emailMessage)
      }

    } catch {
      case e: Exception => {
        log.error("There was a problem sending email message for game_id %s to %s".format(game._id, emailMessage.recipient), e)

        // Update number of attempts
        if (emailMessage.num_attempts < 5) {
          EmailMessage.updateNumAttempts(emailMessage)
        }
      }
    }
  }

  class SMTPAuthenticator extends javax.mail.Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication = {

      val username = SMTP_AUTH_USER
      val password = SMTP_AUTH_PWD

      new PasswordAuthentication(username, password)
    }
  }
}

case class EmailMessage(_id: Long,
                        user_id: Long,
                        game_id: Long,
                        send_at: Long,
                        sent_at: Option[Long],
                        num_attempts: Int,
                        recipient: String,
                        status: String = "unsent")

object EmailMessage {

  def insert(message: EmailMessage) = {
    val dbo = grater[EmailMessage].asDBObject(message)
    MongoManager.emailMessages += dbo
  }

  def findByGameId(game_id: Int): Option[EmailMessage] = {
    val dbObject = MongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[EmailMessage].asObject(o))
  }

  def findByGameIdAndRecipient(game_id: Long, recipient: String): Option[EmailMessage] = {
    val dbObject = MongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id, "recipient" -> recipient))
    dbObject.map(o => grater[EmailMessage].asObject(o))
  }

  def findUnsent(game_id: Long): Iterator[EmailMessage] = {
    val dbObjects = MongoManager.emailMessages.find(
      MongoDBObject("game_id" -> game_id,
                    "status" -> "unsent",
                    "send_at" -> MongoDBObject("$lt" -> DateTime.now().getMillis),
                    "num_attempts" -> MongoDBObject("$lt" -> 5))
    )
    for (x <- dbObjects) yield grater[EmailMessage].asObject(x)
  }

  def markAsSent(message: EmailMessage) = {
    MongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
      $set("sent_at" -> Some(DateTime.now().getMillis),
        "status" -> "sent"
      )
    )
  }

  def updateNumAttempts(message: EmailMessage) = {
    MongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
      $set("num_attempts" -> (message.num_attempts + 1))
    )
  }
}