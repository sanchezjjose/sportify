package util

import java.util.concurrent.TimeUnit
import api.SportifyDbApi
import models._
import play.api.mvc._
import reactivemongo.bson.BSONDocument
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
      val user = User(111111L, "ahouston20@knicks.com", Some("$2a$10$RJuUFQ0vMBr.bJ0haUkKAu0bXKjb824aR7XTUeT9x/KW0A3oNJsmy"), "Allan", "Houston")
      Action.async(request => f(user)(request))
    }
  }

  def withHomepageContext(request: Request[AnyContent], user: User, teamId: Long)(process: (HomepageView) => Result): Future[Result] = {

    for {
      tVm <- buildTeamView(Some(teamId))(user, request)
      seasonId <- tVm.current.season_ids
      currentSeasonOpt <- db.seasonDb.findOne(BSONDocument(SeasonFields.Id -> seasonId, SeasonFields.IsCurrentSeason -> true))
      nextGame <- db.gameDb.findNextGame(currentSeasonOpt.get.game_ids)
      playerInId <- nextGame.get.players_in
      playerOutId <- nextGame.get.players_out
      playerInOpt <- db.playerDb.findOne(BSONDocument(PlayerFields.Id -> playerInId))
      playersInUser <- db.userDb.find(BSONDocument(UserFields.Id -> playerInOpt.get.user_id))
      playerOutOpt <- db.playerDb.findOne(BSONDocument(PlayerFields.Id -> playerOutId))
      playersOutUser <- db.userDb.find(BSONDocument(UserFields.Id -> playerOutOpt.get.user_id))

    } yield {
      process(HomepageView(tVm, nextGame, playersInUser.toSet, playersOutUser.toSet))
    }
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

    for {
      tVm <- buildTeamView(Some(teamId))(user, request)
      seasonId <- tVm.current.season_ids
      currentSeasonOpt <- db.seasonDb.findOne(BSONDocument(SeasonFields.Id -> seasonId, SeasonFields.IsCurrentSeason -> true))
      nextGame <- db.gameDb.findNextGame(currentSeasonOpt.get.game_ids)
      gameId <- currentSeasonOpt.get.game_ids
      games <- db.gameDb.find(BSONDocument(GameFields.Id -> gameId))

    } yield {
      process(ScheduleView(tVm, currentSeasonOpt, games, nextGame))
    }
  }

  def withAccountContext(request: Request[AnyContent], user: User, teamId: Long)(process: (AccountView, PlayerViewModel) => Result): Future[Result] = {

    for {
      tVm <- buildTeamView(Some(teamId))(user, request)
      pVm <- buildPlayerView(Some(teamId))(user, request)

    } yield {
      val accountView = AccountView(
        teams = tVm,
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
  }

  def buildTeamView(teamIdOpt: Option[Long] = None)(implicit user: User, request: Request[AnyContent]): Future[TeamViewModel] = {

    for {
      selectedTeamOpt <- db.teamDb.findOne(BSONDocument(TeamFields.Id -> getTeamId(user, teamIdOpt)))
      teams <- db.teamDb.find(BSONDocument())
      otherTeams = teams.filter(team => team._id != selectedTeamOpt.get._id)

    } yield {
      TeamViewModel(selectedTeamOpt.get.copy(selected = true), otherTeams)
    }
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
