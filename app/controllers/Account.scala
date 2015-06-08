package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.libs.json.Json
import utils.{RequestHelper, Helper}
import views._


object Account extends Controller
  with Helper
  with RequestHelper
  with Secured {

  private var tVm: TeamViewModel = _
  private var pVm: PlayerViewModel = _

	val userForm: Form[AccountView] = Form(
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
        AccountView(tVm.selected_team_id, pVm.id, email, password, firstName, lastName, number, phoneNumber, position, isAdmin)
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

    withAccountContext(request, user, teamId) { (accountView: AccountView, playerViewModel: PlayerViewModel) =>
      pVm = playerViewModel // TODO: remove this awful hack

      render {
        case Accepts.Html() => Ok(views.html.account(userForm.fill(accountView), user.is_admin, buildTeamView(teamId)))
        case Accepts.Json() => Ok(Json.toJson(accountView))
      }
    }
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