package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._


object Login extends Controller {

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password. Hint: team name", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
    })
  )

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.home).withSession("user_info" -> user._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Login.login).withNewSession.flashing(
      "success" -> "You've been successfully logged out."
    )
  }

}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user session variable.
   */
  private def sessionKey(request: RequestHeader) = request.session.get("user_info")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => User => Request[AnyContent] => Result) = Security.Authenticated(sessionKey, onUnauthorized) { key =>

    // First check by email
    User.findByEmail(key).map { user =>

      User.loggedInUser = user
      Action(request => f(user)(request))
    }.getOrElse{

      // Next check by facebook user_id
      User.findByFacebookUserId(key).map { user =>

        println("B: " + user.facebookUser.get.access_token)

        User.loggedInUser = user
        Action(request => f(user)(request))

      }.getOrElse {

        // Finally return onAuthorized
        Action(request => onUnauthorized(request))
      }
    }
  }

}