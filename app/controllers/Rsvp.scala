package controllers

import javax.inject.Inject

import api.MongoManager
import models.GameFields
import models.JsonFormats._
import play.api.libs.json.Json
import play.api.mvc.{Result, Controller, Cookie}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class Rsvp @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new MongoManager(reactiveMongoApi)

  def getFuture[T](futureOptionBlock: Future[Option[T]])(foundBlock: (T => Future[Result])): Future[Result] = {
    futureOptionBlock.flatMap {
      case Some(found) => foundBlock(found)
      case None => Future.successful(NotFound)
    }
  }

  def update(gameId: Long) = isAuthenticatedAsync { implicit userContextFuture => implicit request =>

    getFuture(db.games.findOne(Json.obj(GameFields.Id -> gameId))) { game =>

      userContextFuture.map { userContext =>

        // TODO: remove .get calls here
        val rsvp: Cookie = request.cookies.get("rsvp").get
        val playerId = userContext.player._id

        val updatedGame = if (rsvp.value == "in") {
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

        db.games.update(
          Json.obj(GameFields.Id -> gameId),
          Json.obj("$set" ->
            Json.obj(
              GameFields.PlayersIn -> updatedGame.players_in,
              GameFields.PlayersOut -> updatedGame.players_out
            )
          )
        )

        Ok(Json.toJson(updatedGame))
      }
    }
  }
}
