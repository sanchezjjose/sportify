package controllers

import play.api.mvc._
import models._
import play.api.libs.ws.WS


object Facebook extends Controller with Secured {

  def authenticate(access_token: String, user_id: String) = Action { implicit request =>

    // Async { put code below here and make non-blocking }


    println("A: " + access_token)

    WS.url("https://graph.facebook.com/"+user_id+"?fields=email,first_name,last_name").get().map { response =>

      val json = response.json
      val email = (json \ "email").asOpt[String]
      val firstName = (json \ "first_name").as[String]
      val lastName = (json \ "last_name").asOpt[String]

      // Create facebook entry if one does not already exist
      val fbUserOpt = FacebookUser.findByUserId(user_id)

      // Create new user with facebook credentials or update existing access token
      if (fbUserOpt.isDefined) {
        FacebookUser.updateAccessToken(access_token, user_id)
      } else {
        FacebookUser.insert(access_token, user_id, email, firstName, lastName)
      }
    }

    Redirect(routes.Application.home).withSession("user_info" -> user_id)
  }

  def createEvent(name: String, startTime: String, description: String, location: String) = IsAuthenticated { user => _ =>

    WS.url("https://graph.facebook.com/me/events").post(
      Map("name" -> Seq(name),
          "start_time" -> Seq(startTime),
          "description" -> Seq(description),
          "location" -> Seq(location),
          "privacy_type" -> Seq("SECRET"),
          "access_token" -> Seq(user.facebookUser.get.access_token))
    ).map { response =>


      println("C: " + user.facebookUser.get.access_token)
      println("D: " + User.loggedInUser.facebookUser.get.access_token)

      println(response.json)
    }

    Redirect(routes.Application.home)
  }
}
