package controllers

import play.api.Play.current
import play.api.mvc._
import models._

object Facebook extends Controller with Secured {

  def authenticate(access_token: String, user_id: String) = Action {
    // Create facebook entry if one does not already exist
    FacebookUser.findByAccessToken(access_token).getOrElse {
      FacebookUser.insert(access_token, user_id)
    }

    Ok(views.html.index("Next Game", Game.findNextGame)).withSession("user_id" -> user_id)
  }
}
