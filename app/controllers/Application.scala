package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.home)
  }

  def home = Action {
    Ok(views.html.index("Next Game"))
  }

  def roster = Action {
    Ok(views.html.roster("Gilt Unit Roster"))
  }

  def schedule = Action {
    Ok(views.html.schedule("Schedule"))
  }

  def signUp = Action {
    Ok(views.html.signup("Sign Up"))
  }
}