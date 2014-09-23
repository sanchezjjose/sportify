package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._


case class UserForm (email: String,
                     firstName: String,
                     lastName: String,
                     number: Int,
                     position: String,
                     isAdmin: Boolean)

object Account extends Controller with Teams with Secured {

	val userForm: Form[UserForm] = Form(
		mapping(
      "email" -> email,
			"first_name" -> text,
			"last_name" -> text,
			"number" -> number,
      "position" -> text,
      "is_admin" -> boolean
		)(UserForm.apply)(UserForm.unapply)
	)

  /**
   * NOTE ABOUT FLASH AND WHY 'implicit request' is needed here.
   * http://stackoverflow.com/questions/18560327/could-not-find-implicit-value-for-parameter-flash-play-api-mvc-flash
   *
   * @return
   */
  def account = IsAuthenticated { user => implicit request =>
    val user = User.loggedInUser

    val form = UserForm(email = user.email,
                        firstName = user.first_name,
                        lastName = user.last_name,
                        number = user.player.get.number,
                        position = user.player.get.position,
                        isAdmin = user.is_admin)

    val filledForm = userForm.fill(form)

    Ok(views.html.account(filledForm, user.is_admin, getSelectedTeam(request), getOtherTeams(request)))
  }

  def delete = IsAuthenticated { user => implicit request =>
    User.delete(user)

    Redirect(routes.Login.logout()).flashing(
      "deleted" -> "Your account has been permanently deleted."
    )
  }

  def submit = IsAuthenticated { user => implicit request =>
    userForm.bindFromRequest.fold(

       // Form has errors, re-display it
       errors => BadRequest(html.account(errors, user.is_admin, getSelectedTeam(request), getOtherTeams(request))),

       userFormData => {
         User.update(userFormData)

         val player = User.loggedInUser.player.get
         val updatedPlayer = player.copy(number = userFormData.number, position = userFormData.position)

         val players = getSelectedTeam(request).players
         players -= player
         players += updatedPlayer

         val updatedTeam = getSelectedTeam(request).copy(players = players)
         Team.update(updatedTeam)

         Redirect(routes.Account.account()).flashing(
            "success" -> "Your account information has been successfully updated."
         )
       }
    )
  }
  
}