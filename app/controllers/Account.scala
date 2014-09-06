package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._



case class AccountData (email: String,
                        firstName: String,
                        lastName: String,
                        number: Int,
                        position: String,
                        isAdmin: Boolean)

object Account extends Controller with Secured {

	val accountForm: Form[AccountData] = Form(
		mapping(
      "email" -> email,
			"first_name" -> text,
			"last_name" -> text,
			"number" -> number,
      "position" -> text,
      "is_admin" -> boolean
		)(AccountData.apply)(AccountData.unapply)
	)

  /**
   * NOTE ABOUT FLASH AND WHY 'implicit request' is needed here.
   * http://stackoverflow.com/questions/18560327/could-not-find-implicit-value-for-parameter-flash-play-api-mvc-flash
   *
   * @return
   */
  def account = IsAuthenticated { user => implicit request =>
    val user = User.loggedInUser

    val accountData = AccountData (email = user.email,
                                   firstName = user.first_name,
                                   lastName = user.last_name,
                                   number = user.player.get.number,
                                   position = user.player.get.position,
                                   isAdmin = user.is_admin)

    val filledForm = accountForm.fill(accountData)

    Ok(views.html.account(filledForm, user.is_admin))
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
       errors => BadRequest(html.account(errors, user.is_admin)),

       userFormData => {
         User.update(userFormData)

         Redirect(routes.Account.account()).flashing(
            "success" -> "Your account information has been successfully updated."
         )
       }
    )
  }
  
}