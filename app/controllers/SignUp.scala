package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.mongodb.casbah.Imports._

import views._
import models._

object SignUp extends Controller {

	val signupForm: Form[User] = Form(

		// Define a mapping that will handle Player values
		mapping(
      "_id" -> ignored(new ObjectId()),
			"email" -> email,
			"firstname" -> text,
			"lastname" -> text,
			"number" -> number
		)(User.apply)(User.unapply)
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
       user => {
         User.insert(user)
         // User.create(new User(player.email, "password"))
         Ok(html.signup.summary(user)).withNewSession.flashing(
            "success" -> "Your account has been created. Please login."
         )
       }
    )
  }
  
}