package controllers

import play.api.Play.current
import play.api.mvc._
import models._

object Application extends Controller with Config with Secured {

  def index = Action {
    Redirect(routes.Application.home)
  }

  def home = IsAuthenticated { user => _ =>
    Ok(views.html.index("Next Game", Game.findNextGame))
  }

  def schedule = IsAuthenticated { user => implicit request =>
    Ok(views.html.schedule("Fall 2013", Game.findNextGame, Game.findAll.toList)(user))
  }

  def roster = IsAuthenticated { user => _ =>
    Ok(views.html.roster("Gilt Unit"))
  }

  def news = IsAuthenticated { user => _ =>
    Ok(views.html.news("News & Highlights"))
  }

  def updateScore(game_id: String, result: String, score: String) = Action {
    Game.updateScore(game_id, result, score)
    Redirect(routes.Application.schedule)
  }
}

trait Config {
  val config = play.api.Play.configuration
  lazy val msg = config.getString("msg").getOrElse("Remember to bring your game shirts. Let's get this W!")
}
