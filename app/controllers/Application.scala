package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Redirect(routes.Application.roster)
  }
  
  def roster = Action {
    Ok(views.html.index("Gilt Unit"))
  }
}