package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import views._
import scala.collection.mutable.{Set => MSet}



case class PlayerData(email: String,
                      password: String,
                      firstName: String,
                      lastName: String,
                      jerseyNumber: Int,
                      position: Option[String],
                      teamId: Long)


object SignUp extends Controller with Helper {

	val signupForm: Form[PlayerData] = Form(
		mapping(
      "email" -> email,
      "password" -> nonEmptyText,
			"first_name" -> nonEmptyText,
			"last_name" -> nonEmptyText,
			"number" -> number,
      "position" -> optional(text),
      "team_id" -> longNumber
		)(PlayerData.apply)(PlayerData.unapply)
	)

	/**
   	* Display an empty form.
    */
  def signup = Action { implicit request =>
 	  Ok(html.signup.form(signupForm))
	}

  /**
  * Handle form submission.
  */
  def submit = Action { implicit request =>

     signupForm.bindFromRequest.fold(

       errors => {
         BadRequest(html.signup.form(errors))
       },

       data => {

         Team.findById(data.teamId).map { team =>

           val player = Player(id = generateRandomId(),
                               number = data.jerseyNumber,
                               position = data.position)

           val user = User(_id = generateRandomId(),
                           email = data.email,
                           password = Some(data.password),
                           first_name = data.firstName,
                           last_name = data.lastName,
                           players = MSet(player))

           team.player_ids += player.id

           // Save user and add to team
           User.create(user)
           Team.update(team)

           Ok(html.signup.summary(data)).discardingCookies(DiscardingCookie("team_name")).withNewSession.flashing(
             "success" -> "Your account has been created. Please login."
           )
         }.getOrElse {

           Redirect(routes.SignUp.signup).withNewSession.flashing(
             "failure" -> "Sorry, the team id you entered does not exist."
           )
         }
       }
    )
  }
}