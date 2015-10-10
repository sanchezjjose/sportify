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

    } yield {
        UserContext(user, players.flatten, teams.flatten)
      }

    val result: Future[UserContext] = futureO.future.flatMap { userContextOpt: Option[UserContext] =>

      // handle Option (Future[Option[UserContext]] => Future[UserContext])
      userContextOpt.map(user => Future.successful(user))
        .getOrElse(Future.failed(new RuntimeException("Could not find UserContext")))
    }

    result
  }

  def withHomepageContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: HomepageViewModel => Result): Future[Result] = {

    (for {
      userContext <- liftFO(userContextFuture)
      activeSeason <- FutureO(mongoDb.seasons.findOne(Json.obj(SeasonFields.TeamIds -> Json.obj("$in" -> List(teamId)), SeasonFields.IsActive -> true)))
      nextGameOpt <- liftFO(mongoDb.games.findNextGame(activeSeason.game_ids))

      playersInIds = nextGameOpt.map(_.players_in).getOrElse(Set.empty[Long])
      playersInOpt <- liftFO(Future.traverse(playersInIds)(id => mongoDb.players.findOne(Json.obj(PlayerFields.Id -> id))))
      usersInOpt <- liftFO(Future.traverse(playersInIds)(id => mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> List(id))))))

      playersOutIds = nextGameOpt.map(_.players_out).getOrElse(Set.empty[Long])
      playersOutOpt <- liftFO(Future.traverse(playersOutIds)(id => mongoDb.players.findOne(Json.obj(PlayerFields.Id -> id))))
      usersOutOpt <- liftFO(Future.traverse(playersOutIds)(id => mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> List(id))))))

    } yield {
        val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))
        val player = userContext.getPlayerOnTeam(teamId)

        val pVmsIn = playersInOpt.flatten.flatMap { player =>
          usersInOpt.flatten.map { user =>
            PlayerViewModel(player._id, user.fullName, player.number, user.phone_number, player.position)
          }
        }

        val pVmsOut = playersOutOpt.flatten.flatMap { player =>
          usersOutOpt.flatten.map { user =>
            PlayerViewModel(player._id, user.fullName, player.number, user.phone_number, player.position)
          }
        }

        process(HomepageViewModel(tVm.active_team, tVm.other_teams, nextGameOpt, player._id, pVmsIn, pVmsOut))

      }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.Ok)
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
        val query = mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> List(player._id))))
        val user = Await.result(query, Duration(500, TimeUnit.MILLISECONDS)).get

        PlayerViewModel(player._id, user.fullName, player.number, user.phone_number, player.position)
      }

      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(RosterViewModel(tVm.active_team, tVm.other_teams, pVms.toList.sortBy(p => p.full_name)))

    }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }

  def withScheduleContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: ScheduleViewModel => Result): Future[Result] = {

    (for {
      userContext <- liftFO(userContextFuture)
      activeSeason <- FutureO(mongoDb.seasons.findOne(Json.obj(SeasonFields.TeamIds -> Json.obj("$in" -> List(teamId)), SeasonFields.IsActive -> true)))
      nextGame <- liftFO(mongoDb.games.findNextGame(activeSeason.game_ids))
      games <- liftFO(Future.traverse(activeSeason.game_ids) { id =>
        mongoDb.games.findOne(Json.obj(GameFields.Id -> id))
      })

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(ScheduleViewModel(tVm.active_team, tVm.other_teams, activeSeason, games.flatten.toList, nextGame))

    }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }

  def withAccountContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: AccountViewModel => Result): Future[Result] = {

    userContextFuture.map { userContext =>

      val player = userContext.getPlayerOnTeam(teamId)

      val tVm = TeamViewModel(
        active_team = userContext.getTeam(teamId),
        other_teams = userContext.getOtherTeams(teamId)
      )

      val accountView = AccountViewModel(
        active_team = tVm.active_team,
        other_teams = tVm.other_teams,
        user_id = userContext.user._id,
        player_id = player._id,
        email = userContext.user.email,
        password = userContext.user.password,
        first_name = userContext.user.first_name,
        last_name = userContext.user.last_name,
        number = player.number,
        phone_number = userContext.user.phone_number,
        position = player.position,
        is_admin = userContext.user.is_admin
      )

      process(accountView)
    }
  }
}
