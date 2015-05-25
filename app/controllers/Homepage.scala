package controllers

import models.{Game, Season, Team, User}
import play.api.mvc.Controller
import play.api.libs.json.Json
import utils.Helper


object Homepage extends Controller
  with Secured
  with Helper {

  def index = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView
    Redirect(routes.Homepage.home(tVm.current._id))
  }

  def home(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView(teamId)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGameInSeason = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))
    val playersIn = nextGameInSeason.map(game => game.players_in.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])
    val playersOut = nextGameInSeason.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])

    render {
      case Accepts.Html() => Ok(views.html.index("Next Game", nextGameInSeason, playersIn, playersOut, tVm))
      case Accepts.Json() => Ok(Json.toJson(tVm))
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
