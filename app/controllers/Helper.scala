package controllers

import models._
import play.api.mvc._

import scala.util.Random


trait Helper {

  private[controllers]
  def buildTeamView(implicit user: User, request: Request[AnyContent]): TeamViewModel = {
    val teams = Team.findAllByUser(user)

    TeamViewModel(teams.head, teams.tail)
  }

  private[controllers]
  def buildTeamView(teamId: Long)(implicit user: User, request: Request[AnyContent]): TeamViewModel = {
    val teams = Team.findAllByUser(user)

    Team.findById(teamId).map { currentTeam =>
      TeamViewModel(currentTeam, teams.filter(team => team._id != currentTeam._id))
    }.get
  }

  private[controllers]
  def buildPlayerViews(teamId: Long)(implicit user: User, request: Request[AnyContent]): Set[PlayerViewModel] = {
    (for(playerId <- buildTeamView(teamId).current.player_ids;
         user <- User.findByPlayerId(playerId);
         player <- user.players.find(_.id == playerId)) yield {

      PlayerViewModel(player.id, user.fullName, player.number, player.position)
    }).toSet
  }

  def buildPlayerView(teamId: Long)(implicit user: User, request: Request[AnyContent]): PlayerViewModel = {
    buildPlayerViews(teamId).find(pVm => user.players.exists(_.id == pVm.id)).get
  }

  def generateRandomId(): Long = {
    100000 + Random.nextInt(900000)
  }
}

