package controllers

import models.Game
import play.api.mvc.Controller


trait HomeEndpoints extends Controller with Secured with Helper {

  def changeRsvpStatus(teamId: Long, gameId: Long, status: String) = IsAuthenticated { implicit user => implicit request =>
    val game = Game.findById(gameId).get
    val playerId = buildPlayerView(teamId).id

    if (request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.players_in += playerId
      game.players_out -= playerId
    } else {
      game.players_in -= playerId
      game.players_out += playerId
    }

    Game.update(game)

    Redirect(routes.Application.home(buildTeamView(teamId).current._id))
  }
}
