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

    val game : Option[Game] = Game.findByGameId(9)
    val player = request.session.get("email").get

    // Add user to game.
    game.get.playerIds += User.findByEmail(player).get._id
    Game.update(game.get)
    
    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {
      Ok(Json.toJson(
            Map(
              "status" -> Json.toJson("in"),
              "msg" -> Json.toJson("You are playing. See you there!")
            )
          ))
    } else {
      Ok(Json.toJson(
            Map(
              "status" -> Json.toJson("out"),
              "msg" -> Json.toJson("Ok, you are not playing in this game. Maybe next time!")
            )
          ))
    }
	}
}