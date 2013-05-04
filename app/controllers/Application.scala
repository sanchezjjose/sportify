package controllers

import play.api.mvc._

import models._

object Application extends Controller with Secured {

  def index = Action {
    //Game.loadGames
    Redirect(routes.Application.home)
  }

  def home = IsAuthenticated { user => _ =>
    Ok(views.html.index("Next Game", Game.findNextGame))
  }

  def schedule = IsAuthenticated { user => implicit request =>
    Ok(views.html.schedule("Spring 2013", Game.findNextGame, Game.findAll.toList)(user))
  }

  def roster = IsAuthenticated { user => _ =>
    Ok(views.html.roster("Gilt Unit"))
  }

  def news = IsAuthenticated { user => _ =>
    Ok(views.html.news("News & Highlights"))
  }
}
