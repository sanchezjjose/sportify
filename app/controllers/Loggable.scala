package controllers

trait Loggable {

  val log = play.Logger.of("application")
}
