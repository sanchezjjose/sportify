package utils

trait Loggable {

  val log = play.Logger.of("application")
}
