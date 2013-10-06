package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms.tuple
import play.api.data.Forms.text

object Schedule extends Controller {

  val form = Form(
    tuple("game_id" -> text,
          "status" -> text)
  )

  /**
  * Handle form submission.
  */
	def submit = Action { implicit request =>

    val gameId = request.rawQueryString.split("=")(2).toInt
    val game : Option[Game] = Game.findByGameId(gameId)
    val player = request.session.get("email").get
    val userId = User.findByEmail(player).get._id
    
    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {

      // Add user to game.
      game.get.playersIn += userId
      game.get.playersOut -= userId
      Game.update(game.get)

      Ok(Json.toJson(
            Map(
              "status" -> Json.toJson("in"),
              "msg" -> Json.toJson("You are playing. See you there!")
            )
          ))
    } else {

      // Remove user from game.
      game.get.playersIn -= userId
      game.get.playersOut += userId
      Game.update(game.get)

      Ok(Json.toJson(
            Map(
              "status" -> Json.toJson("out"),
              "msg" -> Json.toJson("Ok, you are not playing in this game. Maybe next time!")
            )
          ))
    }
	}
}