
import models.MailScheduler
import play.api.{Application, GlobalSettings, Logger}

object Global extends GlobalSettings {
  private val log = Logger(this.getClass)

  override def onStart(app: Application) {
    log.info("Starting app %s".format(app))

    val mailScheduler = new MailScheduler
    mailScheduler.startChecking()
    mailScheduler.startSending()
  }

  override def onStop(app: Application) {
    log.info("Stopping app %s".format(app))
  }
}
