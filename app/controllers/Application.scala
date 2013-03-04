package controllers

import play.api._
import play.api.mvc._
import scala.io.Source

import models._

object Application extends Controller with Secured {

  def index = Action {
    Redirect(routes.Login.login)
  }

  def home = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      authenticatedUser.name = user.firstName
      Ok(views.html.index("Next Game"))
    }.getOrElse(Redirect(routes.Login.login))
  }

  def schedule = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user => 
      authenticatedUser.name = user.firstName
      Ok(views.html.schedule("Winter 2013 Season", Schedule.getGames)(user))
    }.getOrElse(Redirect(routes.Login.login))
  }

  def roster = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      authenticatedUser.name = user.firstName
      Ok(views.html.roster("Gilt Unit"))
    }.getOrElse(Redirect(routes.Login.login))
  }

  def news = IsAuthenticated { username => _ =>
    User.findByEmail(username).map { user =>
      authenticatedUser.name = user.firstName
      Ok(views.html.news("News & Highlights"))
    }.getOrElse(Redirect(routes.Login.login))
  }

  object authenticatedUser {
    var name = ""
  }
}
