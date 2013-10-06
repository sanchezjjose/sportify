package controllers

import play.api.mvc._
import models._
import play.api.libs.ws.WS


object Facebook extends Controller with Secured {

  def authenticate(access_token: String, user_id: String) = Action {

    Async {
      WS.url("https://graph.facebook.com/"+user_id+"?fields=email,first_name,last_name").get().map { response =>

        val json = response.json
        val email = (json \ "email").asOpt[String]
        val firstName = (json \ "first_name").as[String]
        val lastName = (json \ "last_name").asOpt[String]

        // Create facebook entry if one does not already exist
        FacebookUser.findByAccessToken(access_token).getOrElse {
          FacebookUser.insert(access_token, user_id, email, firstName, lastName)
        }

//        Ok(response.body).as("application/json")
        Ok(views.html.index("Next Game", Game.findNextGame)).withSession("user_id" -> user_id)
      }
    }
  }
}
