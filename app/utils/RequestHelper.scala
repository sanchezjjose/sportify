package utils

import controllers.Homepage._
import models.{Game, Season, Team, User}
import play.api.mvc.{AnyContent, Result, Request}

import scala.collection.mutable.{Set => MSet}

trait RequestHelper {

  def withContext(request: Request[AnyContent], user: User, teamId: Long)(process: (Set[Team], Option[Game], MSet[User], MSet[User]) => Result): Result = {
    val teams = buildTeams(teamId)(user, request)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGame = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))
    val playersIn = nextGame.map(game => game.players_in.flatMap(id => User.findByPlayerId(id))).getOrElse(MSet.empty[User])
    val playersOut = nextGame.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(MSet.empty[User])

    process(teams, nextGame, playersIn, playersOut)
  }
}
