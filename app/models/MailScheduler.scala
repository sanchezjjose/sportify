package models

import javax.mail._
import javax.mail.internet._
import javax.mail.PasswordAuthentication
import java.util.Properties
import java.util.concurrent.{TimeUnit, Executors}
import akka.dispatch.ExecutionContext
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

/**
 * Handles scheduling of the emails.
 */
class MailScheduler(game: Game)  {

  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor())
  private val scheduler = Executors.newScheduledThreadPool(1)

  scheduler.schedule(new Runnable {
    def run {
      new MailSender().nextGameReminder(game)
    }
  }, MailScheduler.calculateEmailOffset(game), TimeUnit.MILLISECONDS)
}

object MailScheduler {
  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def calculateEmailOffset(game: Game): Long = {
    val sendEmailOffset = format.parseDateTime(game.startTime).minusHours(24).minus(DateTime.now().getMillis)

    println("Scheduling game_id %s for %s hours from now.".format(game.game_id, ((sendEmailOffset.getMillis/1000)/60)/60))

    sendEmailOffset.getMillis
  }
}

/**
 * Handles sending of the emails.
 */
class MailSender {

  def nextGameReminder(game: Game) {

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

    User.findAll.filter(_.email != "").foreach { user =>

      val playersIn = game.playersIn.map { id =>
        "- " + User.findById(id).get.firstName
      }

      val html =
        "You have an upcoming game against '" + game.opponent + "' on " + game.startTime + " <br><br> " +
          "The address is " + game.address + ", New York, New York <br> " +
          "<u>Note</u>: <i> " + game.locationDetails + " </i> <br><br> " +
          "Let us know if you are " +
          "<a href='http://sportify.gilt.com/schedule/rsvp?game_id=" + game.game_id + "&user_id=" + user._id + "&status=in' style='text-decoration: none'><b>IN</b></a> or " +
          "<a href='http://sportify.gilt.com/schedule/rsvp?game_id=" + game.game_id + "&user_id=" + user._id + "&status=out' style='text-decoration: none'><b>OUT</b></a> <br><br>" +
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
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.email))

      println("Sending an email to " + user.email + " for game id " + game.game_id)

//      transport.connect()
//      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
//      transport.close()
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