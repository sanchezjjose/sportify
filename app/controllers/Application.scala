package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.Application.roster)
  }
  
  def roster = Action {
    Ok(views.html.roster("Gilt Unit"))
  }
  
  def schedule = Action {
    Ok(views.html.schedule("Gilt Unit"))
  }
  
  def signUp = Action {
    Ok(views.html.signup("Gilt Unit"))
  }
}