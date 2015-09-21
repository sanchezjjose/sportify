package controllers

import javax.inject.Inject

import api.UserMongoDb
import models.Game
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper

class Rsvp @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val userDb = new UserMongoDb(reactiveMongoApi)

 def update(id: Long) = isAuthenticatedAsync { user => implicit request =>
   (for {
     rsvp: Cookie <- request.cookies.get("rsvp")
     teamId: Cookie <- request.cookies.get("team_id")
     playerId: Long = buildPlayerView(teamId.value.toLong).id
     game: Game <- Game.findById(id)

   } yield {
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

       Game.update(updatedGame)

    }).map { updatedGame =>
      Ok(Json.toJson(updatedGame))

    }.getOrElse {
      BadRequest("The expected content type was not received, or the data provided was invalid.")
    }
  }
}
