package controllers

import javax.inject.Inject

import api.UserMongoDb
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import util.RequestHelper

import scala.concurrent.Future


class Account @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val userDb = new UserMongoDb(reactiveMongoApi)

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
        AccountView(tVm, pVm.id, email, password, firstName, lastName, number, phoneNumber, position, isAdmin)
    } {
      // Data Unbinding
      userForm =>
        Some((userForm.email, userForm.password, userForm.firstName, userForm.lastName,
          userForm.number, userForm.phoneNumber, userForm.position, userForm.isAdmin))
    }
  )

  def account(teamId: Long) = isAuthenticatedAsync { user => implicit request =>
    withAccountContext(request, user, teamId) { (accountView: AccountView, playerViewModel: PlayerViewModel) =>
      pVm = playerViewModel // TODO: REMOVE THIS HACK

      Ok(Json.toJson(accountView))
    }
  }

  def delete = isAuthenticatedAsync { user => implicit request =>
    Future {
      User.delete(user)
      NoContent
    }
  }

  def submit(teamId: Long) = isAuthenticatedAsync { user => implicit request =>
    Future {
      tVm = buildTeamView(teamId)

      userForm.bindFromRequest.fold(
        errors => BadRequest("An error has occurred"),

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
}