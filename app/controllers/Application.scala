package controllers

import models._
import org.slf4j.LoggerFactory
import play.api.mvc._

object Application
  extends Controller
  with Teams
  with Config
  with Secured
  with Loggable {

  val logger = LoggerFactory.getLogger(getClass.getName)

  def index = Action { implicit request =>
    Redirect(routes.Application.home())
  }

  def home = IsAuthenticated { user => implicit request =>
    val selectedTeam = getSelectedTeam(request)
    val currentSeason = selectedTeam.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGameInSeason = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))

    Ok(views.html.index("Next Game", nextGameInSeason,
      selectedTeam, getOtherTeams(request), selectedTeam.playersNeeded))
  }

  def roster = IsAuthenticated { user => implicit request =>
    val selectedTeam = getSelectedTeam(request)
    val users = selectedTeam.players.flatMap(player => User.findByPlayerId(player.id)).toList.sortBy(u => u.first_name)

    Ok(views.html.roster(users, getSelectedTeam(request), getOtherTeams(request)))
  }

  def news = IsAuthenticated { user => implicit request =>

    Ok(views.html.news("News & Highlights", getSelectedTeam(request), getOtherTeams(request)))
  }

  // TODO: move this into a Homepage controller
  // Called from the Homepage via the 'In' and 'Out' buttons
  def changeRsvpStatus(gameId: Long, status: String) = Action { implicit request =>
    val game = Game.findById(gameId).get
    val user = User.loggedInUser

    if (request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.players_in += user.player.get
      game.players_out -= user.player.get
    } else {
      game.players_in -= user.player.get
      game.players_out += user.player.get
    }

    Game.update(game)

    Redirect(routes.Application.home)
  }
}




