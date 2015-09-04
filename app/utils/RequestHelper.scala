package utils

import controllers.Schedule._
import models._
import play.api.mvc.{AnyContent, Result, Request}

import scala.collection.mutable.{Set => MSet}

trait RequestHelper {

  def withHomepageContext(request: Request[AnyContent], user: User, teamId: Long)(process: (HomepageView) => Result): Result = {
    val teams = buildTeams(teamId)(user, request)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGame = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))
    val playersIn = nextGame.map(game => game.players_in.flatMap(id => User.findByPlayerId(id))).getOrElse(MSet.empty[User])
    val playersOut = nextGame.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(MSet.empty[User])

    process(HomepageView(teams, nextGame, playersIn, playersOut))
  }

  def withRosterContext(request: Request[AnyContent], user: User, teamId: Long)(process: (RosterView) => Result): Result = {
    val teams = buildTeams(teamId)(user, request)
    val pVms = buildPlayerViews(teamId)(user, request).toList.sortBy(p => p.name)

    process(RosterView(teams, pVms))
  }

  def withScheduleContext(request: Request[AnyContent], user: User, teamId: Long)(process: (ScheduleView) => Result): Result = {
    val teams = buildTeams(teamId)(user, request)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val games = currentSeason.map(_.game_ids.flatMap(Game.findById).toList.sortBy(_.number)).getOrElse(List.empty[Game])
    val nextGame = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))

    process(ScheduleView(teams, currentSeason, games, nextGame))
  }

  def withAccountContext(request: Request[AnyContent], user: User, teamId: Long)(process: (AccountView, PlayerViewModel) => Result): Result = {
    val teams = buildTeams(teamId)(user, request)
    val pVm = buildPlayerView(teamId)(user, request)
    val accountView = AccountView(teams = teams,
                                  playerId = pVm.id,
                                  email = user.email,
                                  password = user.password,
                                  firstName = user.first_name,
                                  lastName = user.last_name,
                                  number = pVm.number,
                                  phoneNumber = pVm.phoneNumber,
                                  position = pVm.position,
                                  isAdmin = user.is_admin)

    process(accountView, pVm)
  }
}
