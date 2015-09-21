package util

trait Loggable {

  val log = play.Logger.of("application")
}
