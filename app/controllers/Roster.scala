package controllers

import com.sportify.config.Config
import models.RosterView
import play.api.libs.json.Json
import play.api.mvc.Controller
import utils.{Helper, Loggable, RequestHelper}

object Roster extends Controller
  with Helper
  with RequestHelper
  with Config
  with Secured
  with Loggable {

  def roster(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    withRosterContext(request, user, teamId) { rosterView: RosterView =>
      Ok(Json.toJson(rosterView))
    }
  }
}
