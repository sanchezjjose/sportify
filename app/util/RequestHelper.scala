package util

import java.util.concurrent.TimeUnit

import api.{SportifyDbApi, UserDb}
import models._
import play.api.mvc._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait RequestHelper {

  val db: SportifyDbApi

  // TODO: fix and modify so it takes a body parser
  // i.e => Action(parse.text)
  // https://www.playframework.com/documentation/2.4.0/ScalaBodyParsers
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
    val playersIn = nextGame.map(game => game.players_in.flatMap(id => db.userDb.find(BSONDocument("_id" -> BSONObjectID(id.toString)))))
    val playersOut = nextGame.map(game => game.players_out.flatMap(id => User.findByPlayerId(id))).getOrElse(Set.empty[User])

    for {
      tVm <- buildTeamView(Some(teamId))(user, request)
      nextGame <- db.gameDb.findNextGame(tVm.current.)

    } yield {

    }


    process(HomepageView(teams, nextGame, playersIn, playersOut))
  }

  def withRosterContext(request: Request[AnyContent], user: User, teamId: Long)(process: (RosterView) => Result): Future[Result] = {
    for {
      tVm <- buildTeamView(Some(teamId))(user, request)
      pVms <- buildPlayerViews(Some(teamId))(user, request)

    } yield {
      process(RosterView(tVm, pVms.toList.sortBy(p => p.name)))
    }
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

  def buildTeamView(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): Future[TeamViewModel] = {
    (for {
      selectedTeam <- Team.findById( getTeamId(user, teamIdOpt) )
      teams = Team.findAllByUser(user)
      otherTeams = teams.filter(team => team._id != selectedTeam._id)

    } yield {
        TeamViewModel(selectedTeam.copy(selected = true), otherTeams)
      }).get


    val x = (for {
      selectedTeam <- db.teamDb.findOne(BSONDocument(TeamFields.Id -> getTeamId(user, teamIdOpt)))
      teams <- db.teamDb.find(BSONDocument())
      otherTeams = teams.filter(team => team._id != selectedTeam._id)

    } yield {
        TeamViewModel(selectedTeam.copy(selected = true), otherTeams)
      }).get

    x

    val y = db.teamDb.findOne(BSONDocument(TeamFields.Id -> getTeamId(user, teamIdOpt))).map { teamOpt =>
      val selectedTeam = teamOpt.get

      db.teamDb.find(BSONDocument())
    }

    y
  }

  def buildPlayerView(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): Future[PlayerViewModel] = {
    buildPlayerViews(teamIdOpt).map { pVms =>
      pVms.find(pVm => user.player_ids.contains(pVm.id)).get
    }
  }

  private[util] def buildPlayerViews(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): Future[Set[PlayerViewModel]] = {
    for {
      tVm <- buildTeamView(teamIdOpt)
      playerId <- tVm.current.player_ids
      userOpt <- db.userDb.findOne(BSONDocument("$in" -> BSONDocument(TeamFields.PlayerIds -> playerId)))
      playerOpt <- db.playerDb.findOne(BSONDocument(PlayerFields.Id -> playerId))

    } yield for {
      user <- userOpt
      player <- playerOpt

    } yield {
        PlayerViewModel(player.id, user.fullName, player.number, user.phone_number, player.position)
      }
  }

  private[util] def getTeamId(user: User, teamIdOpt: Option[Long]): Long = {

    teamIdOpt.getOrElse {
      user.player_ids.map { playerId =>
        val request = db.teamDb.find(BSONDocument("$in" -> BSONDocument(TeamFields.PlayerIds -> playerId)))
        val teams = Await.result(request, Duration(10, TimeUnit.SECONDS))

        // TODO: should maybe find most recently viewed team / one with upcoming game
        teams.sortBy(_._id).head._id

      }.head
    }
  }
}
