package lib

import com.sendgrid.SendGrid
import com.sendgrid.SendGrid.Email
import util.{Config, Environment, Loggable}
import models.{EmailMessage, Game, Team, User}

class EmailClient extends Loggable with Config {

  private val SMTP_AUTH_USER = Config.sendGridUsername
  private val SMTP_AUTH_PWD  = Config.sendGridPassword

  private val sendGrid = new SendGrid(SMTP_AUTH_USER, SMTP_AUTH_PWD)

  def sendNextGameReminder(emailMessage: EmailMessage, team: Team, game: Game) {

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
