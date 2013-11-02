
import models.{Game, MailScheduler}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.{Application, GlobalSettings, Logger}

import akka.actor.{ActorSystem, Props}

object Global extends GlobalSettings {
  private val log = Logger(this.getClass)

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  override def onStart(app: Application) {
    log.info("Starting app %s".format(app))

    // Schedule email for each game
    Game.findAll.filter( game =>

      // Only schedule emails for upcoming games
      DateTime.now().getMillis < format.parseDateTime(game.startTime).getMillis

    ).foreach(new MailScheduler(_))
  }

  override def onStop(app: Application) {
    log.info("Stopping app %s".format(app))
  }
}
