package controllers

import models._
import org.slf4j.LoggerFactory
import play.api.mvc._

object Application
  extends Controller
  with HomeEndpoints
  with Helper
  with Config
  with Secured
  with Loggable {

  val logger = LoggerFactory.getLogger(getClass.getName)

  def index = Action { implicit request =>
    Redirect(routes.Application.home())
  }

  def home = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView
    val currentSeason = tVm.current.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGameInSeason = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))
    val playersIn = nextGameInSeason.map(game => game.players_in.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])
    val playersOut = nextGameInSeason.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])

    Ok(views.html.index("Next Game", nextGameInSeason, playersIn, playersOut, tVm))
  }

  def roster = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView
    val pVm = buildPlayerViews.toList.sortBy(p => p.name)

    Ok(views.html.roster(pVm, tVm))
  }

  def news = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView

    Ok(views.html.news("News & Highlights", tVm))
  }

  def team(teamName: String) = IsAuthenticated { user => implicit request =>
    Redirect(routes.Application.home()).withCookies(Cookie("team_name", teamName))
  }
}




