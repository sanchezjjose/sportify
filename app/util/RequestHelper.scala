package util

import java.util.concurrent.TimeUnit
import api.MongoManager
import models._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.blocking

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
      val userContextFuture = buildUserContext(userFuture)

      Action.async(request => f(userContextFuture)(request))
    }
  }

  def buildUserContext(userFuture: Future[Option[User]]): Future[UserContext] = {

    // wrap a future into a FutureO
    def liftFO[A](fut: Future[A]) = {
      // convert Future[A] to Future[Option[A]] to conform to FutureO requirements
      val futureOpt: Future[Option[A]] = fut.map(Some(_))
      FutureO(futureOpt)
    }

    val futureO: FutureO[UserContext] = for {
      user <- FutureO(userFuture)
      players <- liftFO(Future.traverse(user.player_ids)(id => db.players.findOne(Json.obj(PlayerFields.Id -> id))))
      teams  <- liftFO(Future.traverse(user.team_ids)(id => db.teams.findOne(Json.obj(TeamFields.Id -> id))))

    } yield UserContext(user, players.flatten, teams.flatten)

    val result: Future[UserContext] = futureO.future flatMap { userContextOpt: Option[UserContext] =>

      // handle Option (Future[Option[UserContext]] => Future[UserContext])
      userContextOpt.map(user => Future.successful(user))
        .getOrElse(Future.failed(new RuntimeException("Could not find UserContext")))
    }

    result
  }

  def withHomepageContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: HomepageViewModel => Result): Future[Result] = {

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

  def withRosterContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: RosterViewModel => Result): Future[Result] = {

    case class ListOfPlayers(playerIds: Set[Long])

    val x =

      for {
        userContext <- userContextFuture

      } yield for {
        playerId <- userContext.getTeam(teamId).player_ids

      } yield for {
          player <- FutureO(db.players.findOne(Json.obj(PlayerFields.Id -> playerId)))
          user <- FutureO(db.users.findOne(Json.obj(UserFields.Id -> 122)))

        } yield {

          PlayerViewModel(
            player._id,
            user.fullName,
            player.number,
            user.phone_number,
            player.position
          )
      }

    val y = x.flatMap(_)

    y

    process(RosterViewModel(tVm, pVms.toList.sortBy(p => p.name)))
  }

  def withScheduleContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: ScheduleViewModel => Result): Future[Result] = {

    for {
      userContext <- userContextFuture
      gameId <- userContext.currentSeasonOpt.game_ids
      games <- db.games.find(Json.obj(GameFields.Id -> gameId))

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      process(ScheduleViewModel(tVm, userContext.currentSeasonOpt, games, userContext.nextGame))
    }
  }

  def withAccountContext(request: Request[AnyContent], userContextFuture: Future[UserContext], teamId: Long)(process: AccountViewModel => Result): Future[Result] = {

    userContextFuture.map { userContext =>

      val tVm = TeamViewModel(
        selectedTeam = userContext.getTeam(teamId),
        otherTeams = userContext.getOtherTeams(teamId)
      )

      val accountView = AccountViewModel(
        teamViewModel = tVm,
        userId = userContext.user._id,
        playerId = userContext.playerOpt.get._id,
        email = userContext.user.email,
        password = userContext.user.password,
        firstName = userContext.user.first_name,
        lastName = userContext.user.last_name,
        number = userContext.playerOpt.get.number,
        phoneNumber = userContext.user.phone_number,
        position = userContext.playerOpt.get.position,
        isAdmin = userContext.user.is_admin
      )

      process(accountView)
    }
  }
}
