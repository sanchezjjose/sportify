package lib

import java.util.concurrent.{Executors, TimeUnit}
import models.{EmailMessage, Team, User}
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import util.{RequestHelper, Helper}


class MailScheduler extends RequestHelper {

  private val emailScheduler = Executors.newScheduledThreadPool(2)

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def startChecking() {

    emailScheduler.scheduleAtFixedRate(new Runnable {

      def run() {
        Logger.info("checking for next game to create email messages...")

        Team.getNextGameByTeam.foreach { case (team, nextGame) =>
          team.player_ids.foreach { playerId =>
            User.findByPlayerId(playerId).foreach { user =>
              val sendAt = format.parseDateTime(nextGame.start_time).minusHours(20).getMillis

              // This is pretty bad. Look into how to use unique index on game id and recipient instead.
              if (!EmailMessage.findByGameIdAndRecipient(nextGame._id, user.email).isDefined) {
                val newMessage = EmailMessage(Helper.generateRandomId(), user._id, nextGame._id, sendAt, None, 0, user.email)
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
        Logger.info("checking for new emails to send...")

        Team.getNextGameByTeam.foreach { case (team, nextGame) =>
          EmailMessage.findUnsent(nextGame._id).foreach { message =>
            new EmailClient().sendNextGameReminder(message, team, nextGame)
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

