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

	// def getGames : List[Game] = {
	// 	val jsonString = Source.fromFile("/web/svc-gilt-sports/app/resources/games.json")
	// 	val json: JsValue = Json.parse(jsonString mkString)
    
 //    val games = (json \ "game") // must return a list to iterate on

 //    val startTime = (json \ "start_time").map(_.as[Date]) // same as Json.fromJson[Date](json \ "start_time")
 //    val address = (json \ "address").map(_.as[String])
 //    val gym = (json \ "gym").map(_.as[String])
 //    val opponent = (json \ "opponent").map(_.as[String])
 //    val result = (json \ "result").map(_.as[String])


	// }

  	/**
    * Handle form submission.
    */
  	def submit = Action { 
      Ok(html.schedule("Winter 2013 Season"))
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