package controllers

import models._
import play.api.mvc._
import scala.util.Random


trait Helper {

  private[controllers]
  def buildTeamView()(implicit user: User, request: Request[AnyContent]): TeamViewModel = {
    val teams = Team.findAllByUser(user)

    getCurrentTeam.filter(currentTeam => teams.exists(team => currentTeam._id == team._id)).map { currentTeam =>
      TeamViewModel(currentTeam, teams.filter(t => t._id != currentTeam._id))
    }.getOrElse {
      TeamViewModel(teams.head, teams.tail)
    }
  }

  private
  def getCurrentTeam()(implicit user: User, request: Request[AnyContent]): Option[Team] = {
    request.cookies.get("team_name").flatMap ( teamName => Team.findByName(teamName.value) )
  }

  private[controllers]
  def buildPlayerViews()(implicit user: User, request: Request[AnyContent]): Set[PlayerViewModel] = {
    (for(playerId <- buildTeamView.current.player_ids;
         user <- User.findByPlayerId(playerId);
         player <- user.players.find(_.id == playerId)) yield {

      PlayerViewModel(player.id, user.fullName, player.number, player.position)
    }).toSet
  }

  def buildPlayerView()(implicit user: User, request: Request[AnyContent]): PlayerViewModel = {
    buildPlayerViews.find(pVm => user.players.exists(_.id == pVm.id)).get
  }

  def generateRandomId(): Long = {
    100000 + Random.nextInt(900000)
  }
}

