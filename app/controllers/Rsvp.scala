package controllers

import javax.inject.Inject
import api.SportifyDbApi
import models.GameFields
import play.api.libs.json.Json
import play.api.mvc.{Controller, Cookie}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONDocument
import util.RequestHelper
import scala.concurrent.ExecutionContext.Implicits.global


class Rsvp @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new SportifyDbApi(reactiveMongoApi)

 def update(gameId: Long) = isAuthenticatedAsync { user => implicit request =>

   // TODO: fix this
   val rsvp: Cookie = request.cookies.get("rsvp").get
   val teamId: Cookie = request.cookies.get("team_id").get

   for {
     gameOpt <- db.gameDb.findOne(BSONDocument(GameFields.Id -> gameId))
     pVm <- buildPlayerView(Some(teamId.value.toLong))

   } yield {
       val game = gameOpt.get // TODO: fix this
       val playerId = pVm.id

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

       db.gameDb.update(
         BSONDocument(GameFields.Id -> gameId),
         BSONDocument("$set" ->
           BSONDocument(
             GameFields.PlayersIn -> updatedGame.players_in,
             GameFields.PlayersOut -> updatedGame.players_out
           )
         )
       )

      Ok(Json.toJson(updatedGame))
    }
  }
}
