package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

object Account extends Controller with Secured {

	val accountForm: Form[User] = Form(
		mapping(
      "_id" -> ignored(""),
      "email" -> email,
			"firstname" -> text,
			"lastname" -> text,
			"number" -> number,
      "position" -> text,
      "facebookUser" -> ignored[Option[FacebookUser]](None),
      "is_admin" -> boolean
		)(User.apply)(User.unapply)
	)

  /**
   * NOTE ABOUT FLASH AND WHY 'implicit request' is needed here.
   * http://stackoverflow.com/questions/18560327/could-not-find-implicit-value-for-parameter-flash-play-api-mvc-flash
   *
   * @return
   */
  def account = IsAuthenticated { user => implicit request =>
    val filledForm = accountForm.fill(User.loggedInUser)

    Ok(views.html.account(filledForm, user.isAdmin))
  }

  def delete = IsAuthenticated { user => implicit request =>
    User.delete(user)

    Redirect(routes.Login.logout()).flashing(
      "deleted" -> "Your account has been permanently deleted."
    )
  }

  def submit = IsAuthenticated { user => implicit request =>
    accountForm.bindFromRequest.fold(
       // Form has errors, re-display it
       errors => BadRequest(html.account(errors, user.isAdmin)),

       updatedUser => {
         User.updateAccountInformation(updatedUser)

         Redirect(routes.Account.account()).flashing(
            "success" -> "Your account information has been successfully updated."
         )
       }
    )
  }
  
}