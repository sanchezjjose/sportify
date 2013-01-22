package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._


object SignUp extends Controller {

	val signupForm: Form[Player] = Form(

		// Define a mapping that will handle Player values
		mapping(
			"username" -> text(minLength = 4),
			"email" -> email,
			"firstname" -> text,
			"lastname" -> text,
			"number" -> text
		)(Player.apply)(Player.unapply)
	)

	/**
   	* Display an empty form.
    */
	def signup = Action {
   	  Ok(html.signup.form(signupForm))
  	}

  	/**
    * Handle form submission.
    */
  	def submit = Action { implicit request =>
     signupForm.bindFromRequest.fold(  
       // Form has errors, redisplay it
       errors => BadRequest(html.signup.form(errors)),
      
       // We got a valid Player value, display the summary
       player => Ok(html.signup.summary(player))
    )
  }
}