package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import scala.io.Source
import play.api.data.Form
import play.api.data.Forms.tuple
import play.api.data.Forms.longNumber
import play.api.data.Forms.text
import scala.collection.mutable.Set
import play.data.DynamicForm

object Schedule extends Controller {

  val form = Form(
    tuple("game_id" -> text,
          "status" -> text)
  )

  /**
  * Handle form submission.
  */
	def submit = Action { implicit request =>

    /**
      update Game document:
      1) use the game_id from request to get the Game object
      2) get user object from currently logged in user -> Application.authenticatedUser.name 
      3) append to List[User] object in the Game object retrieved above
    */


//    def values = form.bindFromRequest.data
//    def status = values("status")
//    def game_id = values("game_id")
//
//    println(status)
//    println(game_id)

    val game : Option[Game] = Game.findByGameId(9)
    println(game.get)
    println(game.get.players)

    println("logged in user -> " + request.session.get("email").get)

    // add the user
    game.get.players += User.findByEmail(request.session.get("email").get).get
    println(game.get.players)
    
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