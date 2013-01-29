package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._


object Login extends Controller {

	/*val loginForm: Form[User] = Form(

		// Define a mapping that will handle Player values
		mapping(
			"username" -> text(minLength = 4),
			"password" -> text
    )(User.apply)(User.unapply)
	)

	/**
 	* Display an empty form.
  */
	def login = Action {
 	  Ok(html.login.form(loginForm))
  }

	/**
  * Handle form submission.
  */
  def submit = Action { implicit request =>
     loginForm.bindFromRequest.fold(  
       // Form has errors, redisplay it
       errors => BadRequest(html.login.form(errors)),
      
       // Valid user, log in successfully
       user => Ok(html.index("Next Game"))
    )
  }*/

  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
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
      user => Redirect(routes.Application.home).withSession("email" -> user._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Login.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Login.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }

}