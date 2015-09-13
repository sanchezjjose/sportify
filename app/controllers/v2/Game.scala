package controllers.v2

import controllers.Secured
import play.api.mvc.{Cookie, Controller}
import models.{ Game => GameModel }
import play.api.libs.json.Json
import utils.{RequestHelper, Loggable, Helper}

object Game extends Controller
  with Helper
  with RequestHelper
  with Loggable
  with Secured {

  def update(id: Long) = IsAuthenticated { implicit user => implicit request =>
   (for {
      rsvpStatus: Cookie <- request.cookies.get("rsvp_status")
      teamId: Cookie <- request.cookies.get("team_id")
      playerId: Long = buildPlayerView(teamId.value.toLong).id
      game: GameModel <- GameModel.findById(id)

    } yield {
       val updatedGame = if (rsvpStatus.value == "in") {
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

       GameModel.update(updatedGame)

    }).map { updatedGame =>
      Ok(Json.toJson(updatedGame))

    }.getOrElse {
      Ok(Json.toJson(
        Map(
          "msg" -> Json.toJson("RESOURCE NOT FOUND!")
        )
      ))
    }
  }
}
