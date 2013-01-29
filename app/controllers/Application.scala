package controllers

import play.api._
import play.api.mvc._
import scala.io.Source

import models._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Login.login)
  }

  def home = Action {
    Ok(views.html.index("Next Game"))
  }

  def schedule = Action {
    Ok(views.html.schedule("Winter 2013 Season"))
  }

  def roster = Action {
    Ok(views.html.roster("Gilt Unit"))
  }

  def news = Action {
    Ok(views.html.news("News & Highlights"))
  }
}
