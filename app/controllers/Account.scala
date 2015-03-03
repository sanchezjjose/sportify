package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._


case class UserForm (playerId: Long,
                     email: String,
                     password: Option[String],
                     firstName: String,
                     lastName: String,
                     number: Int,
                     phoneNumber: Option[String],
                     position: Option[String],
                     isAdmin: Boolean)

object Account extends Controller with Helper with Secured {

  private var tVm: TeamViewModel = _
  private var pVm: PlayerViewModel = _

	val userForm: Form[UserForm] = Form(
		mapping(
      "email" -> email,
      "password" -> optional(text),
			"first_name" -> text,
			"last_name" -> text,
			"number" -> number,
			"phone_number" -> optional(text),
      "position" -> optional(nonEmptyText),
      "is_admin" -> boolean
		) {
      // Data Binding
      (email, password, firstName, lastName, number, phoneNumber, position, isAdmin) =>
        UserForm(pVm.id, email, password, firstName, lastName, number, phoneNumber, position, isAdmin)
    } {
      // Data Unbinding
      userForm =>
        Some((userForm.email, userForm.password, userForm.firstName, userForm.lastName,
              userForm.number, userForm.phoneNumber, userForm.position, userForm.isAdmin))
    }
	)

  /*
   * NOTE: why flash and 'implicit request' is needed here.
   * http://stackoverflow.com/questions/18560327/could-not-find-implicit-value-for-parameter-flash-play-api-mvc-flash
   */
  def account(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    tVm = buildTeamView(teamId)
    pVm = buildPlayerView(teamId)

    val form = UserForm(playerId = pVm.id,
                        email = user.email,
                        password = user.password,
                        firstName = user.first_name,
                        lastName = user.last_name,
                        number = pVm.number,
                        phoneNumber = pVm.phoneNumber,
                        position = pVm.position,
                        isAdmin = user.is_admin)

    val filledForm = userForm.fill(form)

    Ok(views.html.account(filledForm, user.is_admin, tVm))
  }

  def delete = IsAuthenticated { user => implicit request =>
    User.delete(user)

    Redirect(routes.Login.logout()).flashing(
      "deleted" -> "Your account has been permanently deleted."
    )
  }

  def submit(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    tVm = buildTeamView(teamId)

    userForm.bindFromRequest.fold(

       // Form has errors, re-display it
       errors => BadRequest(html.account(errors, user.is_admin, tVm)),

       userFormData => {
         User.update(user, userFormData)
         User.updatePlayer(user, userFormData)

         Redirect(routes.Account.account(teamId)).flashing(
            "success" -> "Your account information has been successfully updated."
         )
       }
    )
  }
  
}