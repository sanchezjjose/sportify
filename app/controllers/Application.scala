package controllers

import play.api.Play.current
import play.api.mvc._
import models._
import org.slf4j.LoggerFactory

object Application extends Controller with Config with Secured with Loggable {

  val logger = LoggerFactory.getLogger(getClass.getName)

  def index = Action {
    Redirect(routes.Application.home())
  }

  def home = IsAuthenticated { user => implicit request =>
    Ok(views.html.index("Next Game", Game.findNextGame))
  }

  def roster = IsAuthenticated { user => _ =>
    val playerInfo = Roster.pullStats.toList.sortBy(s => s._1.firstName)

    Ok(views.html.roster(playerInfo))
  }

  def news = IsAuthenticated { user => _ =>
    Ok(views.html.news("News & Highlights"))
  }

  def updateScore(game_id: String, result: String, score: String) = Action {
    Game.updateScore(game_id, result, score)
    Redirect(routes.Schedule.schedule())
  }
}

trait Config {
  val config = play.api.Play.configuration
}

object Config extends Config {
  lazy val msg = config.getString("msg").getOrElse("Remember to bring your game shirts. Let's get this W!")
  lazy val mongoUrl = config.getString("mongo_url").get
  lazy val environment = config.getString("environment").get
  lazy val fbAppId = config.getString("facebook_app_id").get
  lazy val fbAppSecret = config.getString("facebook_app_secret").get

  //TODO: should be entered together with new games via front-end (maybe a drop down menu)
  lazy val season = "Summer 2014"
}

object Environment {
  val DEVELOPMENT = "development"
  val PRODUCTION = "production"
}
