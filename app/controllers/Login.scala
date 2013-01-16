package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._


object Login extends Controller {

	val loginForm: Form[User] = Form(

		// Define a mapping that will handle Player values
		mapping(
			"username" -> text(minLength = 4),
			"password" -> text
    )(User.apply)(User.unapply)
	)

	/**
 	* Display an empty form.
  */
	def login = Action {
 	  Ok(html.login.form(loginForm))
  }

	/**
  * Handle form submission.
  */
  def submit = Action { implicit request =>
     loginForm.bindFromRequest.fold(  
       // Form has errors, redisplay it
       errors => BadRequest(html.login.form(errors)),
      
       // Valid user, log in successfully
       user => Ok(html.index("Next Game"))
    )
  }
}