package controllers

import play.api._
import play.api.mvc._
import scala.io.Source

object Application extends Controller {

  def index = Action {
    Redirect(routes.Login.login)
  }

  def home = Action {
    Ok(views.html.index("Next Game"))
  }

  def roster = Action {
    Ok(views.html.roster("Gilt Unit Roster", RosterView(Source.fromFile("/web/svc-gilt-sports/app/resources/roster.txt").getLines())))
  }

  def schedule = Action {
    Ok(views.html.schedule("Schedule"))
  }
}

case class RosterView(names: Iterator[String])