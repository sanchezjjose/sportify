package controllers

import api.UserDb
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import util.{Config, Helper}

object Login extends Controller with Helper with Config {

  val loginForm: Form[(String, String)] = {
    Form {
      tuple(
        "email" -> text,
        "password" -> text
      ) verifying("Invalid email or password.", result => result match {
        case (email: String, password: String) => User.authenticate(email, password).isDefined
      })
    }
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Unauthorized("Invalid credentials")
      },
      credentials => {
        implicit val user = User.findByEmail(credentials._1).get
        val defaultTeamId = buildTeamView.current._id
        Redirect(routes.Homepage.home(defaultTeamId))
          .withSession("user_info" -> user.email)
          .flashing("team_id" -> s"$defaultTeamId")
      }
    )
  }
}

trait Secured {

  private def sessionKey(request: RequestHeader): Option[String] = request.session.get("user_info")

  private def onUnauthorized(request: RequestHeader): Result = Results.Unauthorized

  def IsAuthenticated(f: => User => Request[AnyContent] => Result): EssentialAction = {
    Security.Authenticated(sessionKey, onUnauthorized) { email =>
      User.findByEmail(email).map { user =>
        Action(request => f(user)(request))
      }.getOrElse {
        Action(request => onUnauthorized(request))
      }
    }
  }
}