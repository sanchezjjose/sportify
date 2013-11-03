package models

import javax.mail._
import javax.mail.internet._
import javax.mail.PasswordAuthentication
import java.util.Properties
import java.util.concurrent.{TimeUnit, Executors}
import org.joda.time.DateTime
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import controllers.{Loggable, MongoManager}
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.format.DateTimeFormat

/**
 * Handles scheduling of the emails.
 */
class MailScheduler extends Loggable {

  private val emailScheduler = Executors.newScheduledThreadPool(2)

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def startChecking() {
    emailScheduler.scheduleAtFixedRate(new Runnable {
      def run {
        log.info("checking for next game to create email messages...")

        Game.findNextGame.foreach { game =>
          val sendAt = format.parseDateTime(game.startTime).minusHours(20).getMillis
          User.findAll.filter(_.email != "").foreach { user =>

            val newMessage = EmailMessage(user._id, game.game_id, DateTime.now().getMillis, None, 0, user.email)

            //TODO: remove this hack, the insert appears to actually be updating the record for some reason
            EmailMessage.findByGameId(game.game_id).map { message =>
              if (message.sent_at == None) {
                log.info("updating email message " + newMessage)

                EmailMessage.insert(newMessage)
              }
            }.getOrElse {
              log.info("creating new email message " + newMessage)

              EmailMessage.insert(newMessage)
            }
          }
        }
      }
    }, 0, 12, TimeUnit.HOURS)
  }

  def startSending() {
    emailScheduler.scheduleAtFixedRate(new Runnable {
      def run {
        log.info("checking for new emails to send...")

        Game.findNextGame.map { game =>
          EmailMessage.findAll(game.game_id).foreach { message =>
            new MailSender().sendNextGameReminderEmail(message, game)
          }
        }
      }
    }, 1, 120, TimeUnit.MINUTES)
  }
}

/**
 * Handles sending of the emails.
 */
class MailSender extends Loggable {

  def sendNextGameReminderEmail(emailMessage: EmailMessage, game: Game) {

    try {
      val props = new Properties()
      props.put("mail.transport.protocol", "smtp")
      props.put("mail.smtp.host", "smtp.sendgrid.net")
      props.put("mail.smtp.port", "587")
      props.put("mail.smtp.auth", "true")

      val auth = new SMTPAuthenticator()
      val mailSession = Session.getInstance(props, auth)

      // uncomment for debugging infos to stdout
      // mailSession.setDebug(true);

      val transport = mailSession.getTransport()
      val message = new MimeMessage(mailSession)
      val multipart = new MimeMultipart("alternative")

      val userId = emailMessage._id
      val recipient = emailMessage.recipient

      val playersIn = game.playersIn.map { id =>
        "- " + User.findById(id).get.firstName
      }

      val html =
        "You have an upcoming game against '" + game.opponent + "' on " + game.startTime + " <br><br> " +
          "The address is " + game.address.replace(".","") + ", New York, New York <br> " +
          "<u>Note</u>: <i> " + game.locationDetails + " </i> <br><br> " +
          "Let us know if you are " +
          "<a href='http://sportify.gilt.com/schedule/rsvp?game_id=" + game.game_id + "&user_id=" + userId + "&status=in' style='text-decoration: none'><b>IN</b></a> or " +
          "<a href='http://sportify.gilt.com/schedule/rsvp?game_id=" + game.game_id + "&user_id=" + userId + "&status=out' style='text-decoration: none'><b>OUT</b></a> <br><br>" +
          "So far, we have " + playersIn.size +  "  players confirmed: <br> " +
          playersIn.mkString("<br>") + "  <br><br>" +
          "<b>Remember to bring your game shirts, and lets get this W!</b> <br><br>" +
          "<i>Sportify.</i>"

      val part = new MimeBodyPart()
      part.setContent(html, "text/html")

      multipart.addBodyPart(part)

      message.setContent(multipart)
      message.setFrom(new InternetAddress("sportify@email.heroku.com"))
      message.setSubject("You have a basketball game on " + game.startTime)
      message.addRecipient(Message.RecipientType.TO, new InternetAddress("***REMOVED***"))

      log.info("Sending an email to " + recipient + " for game id " + game.game_id)

      transport.connect()
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
      transport.close()

      EmailMessage.markAsSent(emailMessage)

    } catch {
      case e: Exception => {
        log.error("There was a problem sending email message for game_id %s to %s".format(game.game_id, emailMessage.recipient), e)

        // Update number of attempts
        if (emailMessage.num_attempts < 5) {
          EmailMessage.updateNumAttempts(emailMessage)
        }
      }
    }
  }

  class SMTPAuthenticator extends javax.mail.Authenticator {
    override def getPasswordAuthentication(): PasswordAuthentication = {

      //TODO: should be coming from SYSCONFIG
      val username = "***REMOVED***"
      val password = "***REMOVED***"

      new PasswordAuthentication(username, password)
    }
  }
}

case class EmailMessage(_id: String,
                        game_id: Int,
                        send_at: Long,
                        sent_at: Option[Long],
                        num_attempts: Int,
                        recipient: String,
                        status: String = "unsent")

object EmailMessage {

  def insert(message: EmailMessage) = {
    val dbo = grater[EmailMessage].asDBObject(message)
    MongoManager.emailMessagesColl += dbo
  }

  def findByGameId(game_id: Int): Option[EmailMessage] = {
    val dbObject = MongoManager.emailMessagesColl.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[EmailMessage].asObject(o))
  }

  def findAll(game_id: Int): Iterator[EmailMessage] = {
    val dbObjects = MongoManager.emailMessagesColl.find(
      MongoDBObject("game_id" -> game_id,
      "status" -> "unsent",
      "send_at" -> MongoDBObject("$lt" -> DateTime.now().getMillis),
      "num_attempts" -> MongoDBObject("$lt" -> 5))
    )
    for (x <- dbObjects) yield grater[EmailMessage].asObject(x)
  }

  def markAsSent(message: EmailMessage) = {
    MongoManager.emailMessagesColl.update(MongoDBObject("_id" -> message._id),
      $set("sent_at" -> Some(DateTime.now().getMillis),
           "status" -> "sent"
      )
    )
  }

  def updateNumAttempts(message: EmailMessage) = {
    MongoManager.emailMessagesColl.update(MongoDBObject("_id" -> message._id),
      $set("num_attempts" -> (message.num_attempts + 1))
    )
  }
}