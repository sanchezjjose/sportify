package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._
import org.bson.types.ObjectId
import java.util.UUID

object SignUp extends Controller {

	val signupForm: Form[User] = Form(

		// Define a mapping that will handle Player values
		mapping(
      "_id" -> ignored(UUID.randomUUID().toString),
      "email" -> email,
			"firstname" -> nonEmptyText,
			"lastname" -> nonEmptyText,
			"number" -> number,
      "position" -> nonEmptyText,
      "facebookUser" -> ignored[Option[FacebookUser]](None),
      "isAdmin" -> ignored(false)
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
         Ok(html.signup.summary(user)).withNewSession.flashing(
            "success" -> "Your account has been created. Please login."
         )
       }
    )
  }
  
}