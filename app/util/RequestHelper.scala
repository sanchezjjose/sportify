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

    blocking {

      Security.Authenticated(sessionKey, onUnauthorized) { email =>
        val userFuture = db.users.findOne(Json.obj(UserFields.Email -> email))
        val userOpt = Await.result(userFuture, Duration(10, TimeUnit.SECONDS))
        val userContextFuture = buildUserContext(userOpt.get)

        Action.async(request => f(userContextFuture)(request))
      }
    }
  }

  object Test {

    def findId(login: String): Future[Option[Int]] = Future.successful(Option(1))
    def findName(id: Int): Future[Option[String]] = Future.successful(Option("Robert"))
    def findFriends(name: String): Future[Option[List[String]]] = Future.successful(Option(List("loicd", "tpolecat")))

    for {
      id: Option[Int]      <- findId("TunaBoo")
      name: Option[String]    <- id.map(findName).getOrElse(Future.successful(Option.empty))
      friends: Option[List[String]] <- name.map(findFriends).getOrElse(Future.successful(Option.empty))
    } yield friends
  }

  def buildUserContext(user: User): Future[UserContext] = {

    import util.FutureO

//    def findNextGame(season: Season): Future[Option[Game]] = {
//      db.games.findNextGame(season.game_ids)
//    }
//
//    def findPlayersTeams(teamIds: Set[Long]): Future[List[Team]] = {
//
//      val x = teamIds.map { teamId =>
//        db.teams.find(
//          Json.obj(
//            TeamFields.Id -> teamId
//          )
//        )
//      }
//    }

    for {
      teamId <- Future.successful(user.team_ids)

      teams: List[Team] <- db.teams.find(
        Json.obj(
          TeamFields.Id -> teamId
        )
      )

      playerOpt <- FutureO(db.players.findOne(
        Json.obj(
          PlayerFields.UserId -> user._id
        )
      ))

      currentSeasonOpt: Option[Season] <- db.seasons.findOne(
        Json.obj(
          "team_ids" -> Json.obj("$in" -> teams.head._id),
          "is_current" -> true
        )
      )

      nextGameOpt: Option[Game] <- currentSeasonOpt.map(findNextGame).getOrElse(Future.successful(None))

    } yield {

      // 1. get all Players a user has
      // 2. get all team by finding all teams every UserPlayer is on
      // 3. get current Season by checking which season has one of the Players teams, and which one of those is current
      // 4. get next Game by getting all the games of a Season, and checking their dates

      UserContext (
        user = user,
        playerOpt = playerOpt,
        currentSeasonOpt = currentSeasonOpt,
        teams = teams,
        nextGame = nextGameOpt
      )
    }
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

    for {
      userContext <- userContextFuture
      playerId <- userContext.getTeam(teamId).player_ids
      player <- db.players.findOne(Json.obj(PlayerFields.Id -> playerId))
      user <- db.users.findOne(Json.obj(UserFields.Id -> player.get.user_id))

    } yield {
      val tVm = TeamViewModel(userContext.getTeam(teamId), userContext.getOtherTeams(teamId))

      PlayerViewModel(
        player.get._id,
        user.get.fullName,
        player.get.number,
        user.get.phone_number,
        player.get.position
      )
    }

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
