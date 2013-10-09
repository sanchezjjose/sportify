package controllers

import play.api.mvc._
import models._
import play.api.libs.ws.WS
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTimeZone


object Facebook extends Controller with Secured {

  private val appId = "584728524918442"
  private val appSecret = "c2e02f8fbd8a68684ce31e0f091677f7"

  val graphApiBaseUrl = "https://graph.facebook.com/"
  val graphApiCreateEventBaseUrl = graphApiBaseUrl + "me/events?access_token="

  def authenticate(access_token: String, user_id: String) = Action { implicit request =>

  // Async { put code below here and make non-blocking }

    println("A: " + access_token)

    WS.url(graphApiBaseUrl + user_id + "?fields=email,first_name,last_name").get().map { response =>

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

  def createEvent(gameId: Int) = IsAuthenticated { user => _ =>

    Game.findByGameId(gameId).map { game =>

      // Convert between the original and ISO8601 format
      val timeZone = DateTimeZone.forID("America/New_York")
      val format = DateTimeFormat.forPattern("E MM/dd/yyyy, HH:mm aa")
      val dateTime = format.parseDateTime(game.startTime).withZone(timeZone)

      // Create the ISO8601 format style and set the specific hour and day for the event
      val iso8601Format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      val isoStartTime = dateTime.withHourOfDay(16).minusDays(1).toString(iso8601Format)

      // The data that will be posted to FB
      val data = Map("name" -> Seq("Game Tomorrow: Gilt Unit vs. %s".format(game.opponent)),
        "start_time" -> Seq(isoStartTime),
        "description" -> Seq("You have a basketball game tomorrow, %s. Remember to bring your shirt.".format(game.startTime)),
        "location" -> Seq(game.address + ", New York, New York"),
        "privacy_type" -> Seq("SECRET"))

      WS.url(graphApiCreateEventBaseUrl + User.loggedInUser.facebookUser.get.access_token).post(data).map { response =>

        println("C: " + user.facebookUser.get.access_token)
        println("D: " + User.loggedInUser.facebookUser.get.access_token)

        // TODO: check response for error, and generate new access_token here if expired
        println(response.json)

//        if (response.getAHCResponse.getStatusCode != 200) {
//          "https://graph.facebook.com/oauth/access_token?client_id=%s&redirect_uri=%s&client_secret=%s&code=%s"
//            .format(appId, "@routes.createEvent("+gameId+")", appSecret, "190")
//        }
      }
    }

    Redirect(routes.Application.home).flashing("success" -> "You will be reminded by facebook 1 day prior to your next game!")
  }
}