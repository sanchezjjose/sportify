package util

import java.util.concurrent.TimeUnit

import api.MongoManager
import models._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import FutureO._


trait RequestHelper {

  val mongoDb: MongoManager

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
      val userFuture = mongoDb.users.findOne(Json.obj(UserFields.Email -> email))
      val userContextFuture = buildUserContext(userFuture)

      Action.async(request => f(userContextFuture)(request))
    }
  }

  def buildUserContext(userFuture: Future[Option[User]]): Future[UserContext] = {

    val futureO: FutureO[UserContext] = for {
      user <- FutureO(userFuture)
      players <- liftFO(Future.traverse(user.player_ids)(id => mongoDb.players.findOne(Json.obj(PlayerFields.Id -> id))))
      teams  <- liftFO(Future.traverse(user.team_ids)(id => mongoDb.teams.findOne(Json.obj(TeamFields.Id -> id))))

    } yield UserContext(user, players.flatten, teams.flatten)

    val result: Future[UserContext] = futureO.future flatMap { userContextOpt: Option[UserContext] =>

      // handle Option (Future[Option[UserContext]] => Future[UserContext])
      userContextOpt.map(user => Future.successful(user))
        .getOrElse(Future.failed(new RuntimeException("Could not find UserContext")))
    }

    result
  }

  def withHomepageContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: HomepageViewModel => Result): Future[Result] = {

    (for {
      userContext <- liftFO(userContextFuture)
      activeSeason <- FutureO(mongoDb.seasons.findOne(Json.obj(SeasonFields.TeamIds -> Json.obj("$in" -> teamId), SeasonFields.IsCurrent -> true)))
      nextGameOpt <- liftFO(mongoDb.games.findNextGame(activeSeason.game_ids))

    } yield {
        val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

        process(HomepageViewModel(tVm, nextGameOpt))

      }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }

  def withRosterContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: RosterViewModel => Result): Future[Result] = {

    (for {
      userContext <- liftFO(userContextFuture)
      team <- FutureO(mongoDb.teams.findOne(Json.obj(TeamFields.Id -> teamId)))
      players <- liftFO(Future.traverse(team.player_ids)(id => mongoDb.players.findOne(Json.obj(PlayerFields.Id -> id))))

    } yield {

      // TODO: remove blocking call
      val pVms = players.flatten.map { player =>
        val query = mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> player._id)))
        val user = Await.result(query, Duration(500, TimeUnit.MILLISECONDS)).get

        PlayerViewModel(player._id, user.fullName, player.number, user.phone_number, player.position)
      }

      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(RosterViewModel(tVm, pVms.toList.sortBy(p => p.name)))

    }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }

  def withScheduleContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: ScheduleViewModel => Result): Future[Result] = {

    (for {
      userContext <- liftFO(userContextFuture)
      activeSeason <- FutureO(mongoDb.seasons.findOne(Json.obj(SeasonFields.TeamIds -> Json.obj("$in" -> teamId), SeasonFields.IsCurrent -> true)))
      nextGame <- liftFO(mongoDb.games.findNextGame(activeSeason.game_ids))
      games <- liftFO(Future.traverse(activeSeason.game_ids) { id =>
        mongoDb.games.findOne(Json.obj(GameFields.Id -> id))
      })

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(ScheduleViewModel(tVm, activeSeason, games.flatten.toList, nextGame))

    }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }

  def withAccountContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: AccountViewModel => Result): Future[Result] = {

    userContextFuture.map { userContext =>

      val player = userContext.getPlayerOnTeam(teamId)

      val tVm = TeamViewModel(
        selectedTeam = userContext.getTeam(teamId),
        otherTeams = userContext.getOtherTeams(teamId)
      )

      val accountView = AccountViewModel(
        teamViewModel = tVm,
        userId = userContext.user._id,
        playerId = player._id,
        email = userContext.user.email,
        password = userContext.user.password,
        firstName = userContext.user.first_name,
        lastName = userContext.user.last_name,
        number = player.number,
        phoneNumber = userContext.user.phone_number,
        position = player.position,
        isAdmin = userContext.user.is_admin
      )

      process(accountView)
    }
  }
}
