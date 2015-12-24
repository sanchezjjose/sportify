package controllers

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import api.MongoManager
import models.{RsvpViewModel, UserFields, PlayerViewModel, GameFields}
import models.JsonFormats._
import play.api.libs.json.Json
import play.api.mvc.{Results, Result, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.{FutureO, RequestHelper}
import FutureO._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class Rsvp @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val mongoDb = new MongoManager(reactiveMongoApi)

  def getFuture[T](futureOptionBlock: Future[Option[T]])(foundBlock: (T => Future[Result])): Future[Result] = {
    futureOptionBlock.flatMap {
      case Some(found) => foundBlock(found)
      case None => Future.successful(NotFound)
    }
  }

  def update(playerId: Long, gameId: Long) = isAuthenticatedAsync { implicit userContextFuture => implicit request =>

    (for {
      userContext <- liftFO(userContextFuture)
      rsvpCookie <- FutureO(Future(request.cookies.get("rsvp")))
      game <- FutureO(mongoDb.games.findOne(Json.obj(GameFields.Id -> gameId)))

    } yield {
      val updatedGame = if (rsvpCookie.value == "in") {
        game.copy(
          players_in = game.players_in + playerId,
          players_out = game.players_out - playerId
        )

      } else {
        game.copy(
          players_in = game.players_in - playerId,
          players_out = game.players_out + playerId
        )
      }

      mongoDb.games.update(
        Json.obj(GameFields.Id -> gameId),
        Json.obj("$set" ->
          Json.obj(
            GameFields.PlayersIn -> updatedGame.players_in,
            GameFields.PlayersOut -> updatedGame.players_out
          )
        )
      )

      // TODO: remove blocking calls
      val pVmsIn = updatedGame.players_in.map { playerId =>
        val query = mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> List(playerId))))
        val user = Await.result(query, Duration(500, TimeUnit.MILLISECONDS)).get

        PlayerViewModel(playerId, user.fullName, 0, user.phone_number, None)
      }

      val pVmsOut = updatedGame.players_out.map { playerId =>
        val query = mongoDb.users.findOne(Json.obj(UserFields.PlayerIds -> Json.obj("$in" -> List(playerId))))
        val user = Await.result(query, Duration(500, TimeUnit.MILLISECONDS)).get

        PlayerViewModel(playerId, user.fullName, 0, user.phone_number, None)
      }

      Ok(Json.obj("players_in" -> pVmsIn, "players_out" -> pVmsOut))

    }).future.flatMap {

      case Some(result) => Future.successful(result)
      case None => Future.successful(Results.NotFound)
    }
  }
}
