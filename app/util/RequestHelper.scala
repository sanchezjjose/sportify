package util

import api.UserDb
import models._
import play.api.mvc._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future

trait RequestHelper {

  val userDb: UserDb

  def isAuthenticatedAsync(f: => User => Request[AnyContent] => Future[Result]): EssentialAction = {

    def sessionKey(request: RequestHeader): Option[String] = request.session.get("user_info")

    def onUnauthorized(request: RequestHeader): Result = Results.Unauthorized

    Security.Authenticated(sessionKey, onUnauthorized) { email =>
      val user = User(111111L, "sanchezjjose@gmail.com", None, "Jose", "Sanchez")
      Action.async(request => f(user)(request))
    }
  }

  def withHomepageContext(request: Request[AnyContent], user: User, teamId: Long)(process: (HomepageView) => Result): Future[Result] = {
    val teams = buildTeamView(Some(teamId))(user, request)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val nextGame = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))
    val playersIn = nextGame.map(game => game.players_in.flatMap(id => userDb.find(BSONDocument("_id" -> BSONObjectID(id.toString)))))
    val playersOut = nextGame.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])

    process(HomepageView(teams, nextGame, playersIn, playersOut))
  }

  def withRosterContext(request: Request[AnyContent], user: User, teamId: Long)(process: (RosterView) => Result): Future[Result] = {
    val teams = buildTeamView(Some(teamId))(user, request)
    val pVms = buildPlayerViews(Some(teamId))(user, request).toList.sortBy(p => p.name)

    process(RosterView(teams, pVms))
  }

  def withScheduleContext(request: Request[AnyContent], user: User, teamId: Long)(process: (ScheduleView) => Result): Future[Result] = {
    val teams = buildTeamView(Some(teamId))(user, request)
    val currentSeason = Team.findById(teamId).get.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val games = currentSeason.map(_.game_ids.flatMap(Game.findById).toList.sortBy(_.number)).getOrElse(List.empty[Game])
    val nextGame = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))

    process(ScheduleView(teams, currentSeason, games, nextGame))
  }

  def withAccountContext(request: Request[AnyContent], user: User, teamId: Long)(process: (AccountView, PlayerViewModel) => Result): Future[Result] = {
    val teams = buildTeamView(Some(teamId))(user, request)
    val pVm = buildPlayerView(Some(teamId))(user, request)
    val accountView = AccountView(
      teams = teams,
      playerId = pVm.id,
      email = user.email,
      password = user.password,
      firstName = user.first_name,
      lastName = user.last_name,
      number = pVm.number,
      phoneNumber = pVm.phoneNumber,
      position = pVm.position,
      isAdmin = user.is_admin
    )

    process(accountView, pVm)
  }

  def buildTeamView(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): TeamViewModel = {
    (for {
      selectedTeam <- Team.findById( getTeamId(user, teamIdOpt) )
      teams = Team.findAllByUser(user)
      otherTeams = teams.filter(team => team._id != selectedTeam._id)

    } yield {
        TeamViewModel(selectedTeam.copy(selected = true), otherTeams)
      }).get
  }

  def buildPlayerView(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): PlayerViewModel = {
    buildPlayerViews(teamIdOpt).find(pVm => user.players.exists(_.id == pVm.id)).get
  }

  def buildPlayerViews(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): Set[PlayerViewModel] = {
    for {
      playerId <- buildTeamView(teamIdOpt).current.player_ids
      user <- userDb.findByPlayerId(playerId)
      player <- user.players.find(_.id == playerId)

    } yield {
      PlayerViewModel(player.id, user.fullName, player.number, user.phone_number, player.position)
    }
  }

  private[util] def getTeamId(user: User, teamIdOpt: Option[Long]): Long = {
    teamIdOpt.getOrElse(Team.findAllByUser(user).head._id)
  }
}
