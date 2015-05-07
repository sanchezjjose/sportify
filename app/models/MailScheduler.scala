package models

import java.util.concurrent.{Executors, TimeUnit}

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.sendgrid.SendGrid
import com.sendgrid.SendGrid.Email
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
    user.email.trim != ""
  }
}

/**
 * Handles sending of the emails.
 */
class MailSender extends Loggable with Config {

  private val SMTP_AUTH_USER = Config.sendGridUsername
  private val SMTP_AUTH_PWD  = Config.sendGridPassword

  private val sendGrid = new SendGrid(SMTP_AUTH_USER, SMTP_AUTH_PWD)

  def sendNextGameReminderEmail(emailMessage: EmailMessage, team: Team, game: Game) {

    try {
      val recipient = emailMessage.recipient

      val isEmailable = {
        (Config.environment == Environment.DEVELOPMENT && recipient == Config.testEmail) ||
         Config.environment == Environment.PRODUCTION
      }

      log.debug(s"Email $recipient: $isEmailable")

      if (isEmailable) {
        val playersIn = game.players_in.map("- " + User.findByPlayerId(_).get.first_name)
        val userId = emailMessage.user_id
        val html = views.html.email.reminderEmail(team, game, userId, playersIn).toString()

        val email = new Email()
        email.addSmtpApiTo(recipient)
        email.setFrom(Config.fromEmail)
        email.setSubject("You have an upcoming game on " + game.start_time)
        email.setHtml(html)

        val response = sendGrid.send(email)

        log.info(s"Response : ${response.getMessage}")
        log.info(s"Sending an email to $recipient for game id ${game._id}")

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

  private val mongoManager = MongoManagerFactory.instance

  def insert(message: EmailMessage) = {
    val dbo = grater[EmailMessage].asDBObject(message)
    mongoManager.emailMessages += dbo
  }

  def findByGameId(game_id: Int): Option[EmailMessage] = {
    val dbObject = mongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[EmailMessage].asObject(o))
  }

  def findByGameIdAndRecipient(game_id: Long, recipient: String): Option[EmailMessage] = {
    val dbObject = mongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id, "recipient" -> recipient))
    dbObject.map(o => grater[EmailMessage].asObject(o))
  }

  def findUnsent(game_id: Long): Iterator[EmailMessage] = {
    val dbObjects = mongoManager.emailMessages.find(
      MongoDBObject("game_id" -> game_id,
                    "status" -> "unsent",
                    "send_at" -> MongoDBObject("$lt" -> DateTime.now().getMillis),
                    "num_attempts" -> MongoDBObject("$lt" -> 5))
    )
    for (x <- dbObjects) yield grater[EmailMessage].asObject(x)
  }

  def markAsSent(message: EmailMessage) = {
    mongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
      $set("sent_at" -> Some(DateTime.now().getMillis),
        "status" -> "sent"
      )
    )
  }

  def updateNumAttempts(message: EmailMessage) = {
    mongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
      $set("num_attempts" -> (message.num_attempts + 1))
    )
  }
}