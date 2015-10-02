package util

import java.util.concurrent.TimeUnit
import api.MongoManager
import models._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global


trait RequestHelper {

  val db: MongoManager

  /*
   * TODO: fix and modify so it takes a body parser
   * https://www.playframework.com/documentation/2.4.0/ScalaBodyParsers -- e.g., Action(parse.text)
   */
  def isAuthenticatedAsync(f: => Future[UserContext] => Request[AnyContent] => Future[Result]): EssentialAction = {

    def sessionKey(request: RequestHeader): Option[String] = {
      request.session.get("user_info")
    }

    def onUnauthorized(request: RequestHeader): Result = {
      Results.Unauthorized
    }

    Security.Authenticated(sessionKey, onUnauthorized) { email =>
      val userFuture = db.users.findOne(Json.obj(UserFields.Email -> email))
      val userOpt = Await.result(userFuture, Duration(10, TimeUnit.SECONDS))
      val userContextFuture = buildUserContext(userOpt.get)

      Action.async(request => f(userContextFuture)(request))
    }
  }

  def buildUserContext(user: User): Future[UserContext] = {

    for {
      // TODO: how to know which is selected team? Team object might still need an is_selected boolean.
      teams <- Future(List(Team(111111, "test", Set.empty[Long], Sport(111111, "Basketball", ""))))
      currentSeasonOpt <- db.seasons.findOne(Json.obj("team_ids" -> Json.obj("$in" -> teams.head._id), "is_current" -> true))
      nextGameOpt <- db.games.findNextGame(currentSeasonOpt.get.game_ids)
      playerId = teams.head.player_ids.find(playerId => playerId == user._id).get
      playerOpt <- db.players.findOne(Json.obj(PlayerFields.Id -> playerId))

    } yield {

      UserContext (
        user = user,
        player = playerOpt.get,
        currentSeason = currentSeasonOpt.get,
        teams = teams,
        nextGame = nextGameOpt,
        sport = teams.head.sport
      )
    }
  }

  def withHomepageContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: (HomepageViewModel) => Result): Future[Result] = {

    for {
      userContext <- userContextFuture
      playerInId <- userContext.nextGame match { case Some(game) => game.players_in case None => Set.empty[Long] }
      playerOutId <- userContext.nextGame match { case Some(game) => game.players_out case None => Set.empty[Long] }
      playerInOpt <- db.players.findOne(Json.obj(PlayerFields.Id -> playerInId))
      playersInUser <- db.users.find(Json.obj(UserFields.Id -> playerInOpt.get.user_id))
      playerOutOpt <- db.players.findOne(Json.obj(PlayerFields.Id -> playerOutId))
      playersOutUser <- db.users.find(Json.obj(UserFields.Id -> playerOutOpt.get.user_id))

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(HomepageViewModel(tVm, userContext.nextGame, playersInUser.toSet, playersOutUser.toSet))
    }
  }

  def withRosterContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: (RosterViewModel) => Result): Future[Result] = {

    val x = for {
      userContext <- userContextFuture
      playerId <- userContext.getTeam(teamId).player_ids
      player <- db.players.findOne(Json.obj(PlayerFields.Id -> playerId))
      user <- db.users.findOne(Json.obj(UserFields.Id -> player.get.user_id))

    } yield {
      PlayerViewModel(
        player.get._id,
        user.get.fullName,
        player.get.number,
        user.get.phone_number,
        player.get.position
      )
    }



    userContextFuture.map { userContext =>
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      userContext.getTeam(teamId).player_ids.map { playerId =>
        db.users.findOne(Json.obj(UserFields.Id -> ))
      }

      val pVms = PlayerViewModel(
        userContext.player._id,
        userContext.user.fullName,
        userContext.player.number,
        userContext.user.phone_number,
        userContext.player.position
      )

      process(RosterViewModel(tVm, pVms.toList.sortBy(p => p.name)))
    }
  }

  def withScheduleContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: (ScheduleViewModel) => Result): Future[Result] = {

    for {
      userContext <- userContextFuture
      gameId <- userContext.currentSeason.game_ids
      games <- db.games.find(Json.obj(GameFields.Id -> gameId))

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(ScheduleViewModel(tVm, userContext.currentSeason, games, userContext.nextGame))
    }
  }

  def withAccountContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: (AccountViewModel) => Result): Future[Result] = {

    userContextFuture.map { userContext =>

      val tVm = TeamViewModel(
        selectedTeam = userContext.getTeam(teamId),
        otherTeams = userContext.getOtherTeams(teamId)
      )

      val accountView = AccountViewModel(
        teamViewModel = tVm,
        userId = userContext.user._id,
        playerId = userContext.player._id,
        email = userContext.user.email,
        password = userContext.user.password,
        firstName = userContext.user.first_name,
        lastName = userContext.user.last_name,
        number = userContext.player.number,
        phoneNumber = userContext.user.phone_number,
        position = userContext.player.position,
        isAdmin = userContext.user.is_admin
      )

      process(accountView)
    }
  }
}
