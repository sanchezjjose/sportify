package controllers

import models._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.mvc._

object Application extends Controller with Config with Secured with Loggable {

  val logger = LoggerFactory.getLogger(getClass.getName)

  def index = Action {
    Redirect(routes.Application.home())
  }

  def home = IsAuthenticated { user => implicit request =>
    Ok(views.html.index("Next Game", Game.findNextGame))
  }

  def roster = IsAuthenticated { user => _ =>
    val users = User.findAll.toList.sortBy(u => u.first_name)

    Ok(views.html.roster(users))
  }

  def news = IsAuthenticated { user => _ =>
    Ok(views.html.news("News & Highlights"))
  }

  // TODO: move this into a Homepage controller
  // Called from the Homepage via the 'In' and 'Out' buttons
  def changeRsvpStatus(gameId: Long, status: String) = Action { implicit request =>
//    val gameId = request.rawQueryString.split("=")(2).toInt
    val game: Game = Game.findById(gameId).get
    val user = User.loggedInUser

    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.players_in += user.player.get
      game.players_out -= user.player.get
    } else {
      game.players_in -= user.player.get
      game.players_out += user.player.get
    }

    Game.update(game)

    Redirect(routes.Application.home())
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
}

object Environment {
  val DEVELOPMENT = "development"
  val PRODUCTION = "production"
}
