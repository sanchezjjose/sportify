package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import util.Helper

case class PlayerData(
                       email: String,
                       password: String,
                       firstName: String,
                       lastName: String,
                       jerseyNumber: Int,
                       position: Option[String],
                       phoneNumber: Option[String],
                       teamId: Long
                       )

object SignUp extends Controller with Helper {

  val signupForm: Form[PlayerData] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "first_name" -> nonEmptyText,
      "last_name" -> nonEmptyText,
      "number" -> number,
      "position" -> optional(text),
      "phone_number" -> optional(text),
      "team_id" -> longNumber
    )(PlayerData.apply)(PlayerData.unapply)
  )

  def submit = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => {
        BadRequest
      },
      data => {
        val teamId = data.teamId

        Team.findById(teamId).map { team =>

          val player = Player(
            id = generateRandomId(),
            number = data.jerseyNumber,
            position = data.position
          )

          val user = User(
            _id = generateRandomId(),
            email = data.email,
            password = Some(data.password),
            first_name = data.firstName,
            last_name = data.lastName,
            players = Set(player),
            phone_number = data.phoneNumber
          )

          val updatedTeam = team.copy(player_ids = team.player_ids + player.id)

          User.create(user)

          Team.update(updatedTeam)

          Redirect(routes.Homepage.home(teamId))
            .withSession("user_info" -> user.email)
            .flashing("team_id" -> s"$teamId")

        }.getOrElse(BadRequest)
      })
  }
}