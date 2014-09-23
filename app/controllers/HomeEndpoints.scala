package controllers

import models.{User, Game}
import play.api.mvc.{Action, Controller}


trait HomeEndpoints extends Controller {

  def changeRsvpStatus(gameId: Long, status: String) = Action { implicit request =>
    val game = Game.findById(gameId).get
    val user = User.loggedInUser

    if (request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      game.players_in += user.player.get
      game.players_out -= user.player.get
    } else {
      game.players_in -= user.player.get
      game.players_out += user.player.get
    }

    Game.update(game)

    Redirect(routes.Application.home)
  }
}
