package controllers

import models.RosterView
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{Config, Helper, Loggable, RequestHelper}

object Roster extends Controller
  with Helper
  with RequestHelper
  with Config
  with Secured
  with Loggable {

  def roster(teamId: Long) = Action { /*implicit user*/ => implicit request =>
    withRosterContext(request, user, teamId) { rosterView: RosterView =>
      Ok(Json.toJson(rosterView))
    }
  }
}
