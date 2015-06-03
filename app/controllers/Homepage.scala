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
    withContext(request, user, teamId) { (teams: Set[Team], nextGame: Option[Game], playersIn: MSet[User], playersOut: MSet[User]) =>
      render {
        case Accepts.Html() => Ok(views.html.index("Next Game", nextGame, playersIn, playersOut, buildTeamView(teamId)))
        case Accepts.Json() => Ok(Json.toJson(HomepageView(teamId, teams, nextGame, playersIn, playersOut)))
      }
    }
  }

  def changeRsvpStatus(teamId: Long, gameId: Long, status: String) = IsAuthenticated { implicit user => implicit request =>
    val game = Game.findById(gameId).get
    val playerId = buildPlayerView(teamId).id

    if (request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.players_in += playerId
      game.players_out -= playerId
    } else {
      game.players_in -= playerId
      game.players_out += playerId
    }

    Game.update(game)

    Redirect(routes.Homepage.home(buildTeamView(teamId).current._id))
  }
}
