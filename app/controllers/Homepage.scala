package controllers

import models._
import play.api.libs.json.Json
import play.api.mvc.Controller
import utils.{Helper, RequestHelper}

import scala.collection.mutable.{Set => MSet}

object Homepage extends Controller
  with Secured
  with Helper
  with RequestHelper {

  def index = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView
    Redirect(routes.Homepage.home(tVm.current._id))
  }

  def home(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    withHomepageContext(request, user, teamId) { homepageView: HomepageView =>
      render {
        case Accepts.Html() => Ok(views.html.index("Next Game", homepageView.nextGameInSeason, homepageView.playersIn, homepageView.playersOut, buildTeamView(teamId)))
        case Accepts.Json() => Ok(Json.toJson(homepageView))
      }
    }
  }

  def changeRsvpStatus(teamId: Long, gameId: Long, status: String) = IsAuthenticated { implicit user => implicit request =>
    val game = Game.findById(gameId).get
    val playerId = buildPlayerView(teamId).id

    val updatedGame = if (request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.copy(
        players_in = game.players_in + playerId,
        players_out = game.players_out - playerId
      )

    } else {
      game.copy(
        players_in = game.players_in - playerId,
        players_out = game.players_out + playerId
      )
    }

    Game.update(updatedGame)

    Redirect(routes.Homepage.home(buildTeamView(teamId).current._id))
  }
}
