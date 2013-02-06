package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

import play.api.libs.json._

import scala.io.Source

import java.util.Date

object Schedule extends Controller {

	def getGames : List[Game] = {
		val jsonString = Source.fromFile("/web/svc-gilt-sports/app/resources/games.json")
		val json: JsValue = Json.parse(jsonString mkString)
    
    val games = (json \ "games").as[List[JsObject]] // must return a list to iterate on

    games.map { game =>
      Game(startTime = (game \ "start_time").as[String],
            address = (game \ "address").as[String],
            gym = (game \ "gym").as[String],
            opponent = (game \ "opponent").as[String],
            result = (game \ "result").as[String])
    }
	}

  	/**
    * Handle form submission.
    */
  	def submit = Action { 
      Ok(html.schedule("Winter 2013 Season", getGames))
  	}

  	/**
  * Handle form submission.
  */
  // def submit = Action { implicit request =>
  //    signupForm.bindFromRequest.fold(  
  //      // Form has errors, redisplay it
  //      errors => BadRequest(html.signup.form(errors)),
      
  //      // We got a valid Player value, display the summary
  //      game => {
  //        Game.insert(game)
  //        Ok(html.schedule("Winter 2013 Season"))
  //      }
  //   )
  // }
}