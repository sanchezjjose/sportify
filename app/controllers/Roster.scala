package controllers

import com.sportify.config.Config
import models.{RosterView, PlayerViewModel}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.{RequestHelper, Loggable, Helper}

object Roster extends Controller
  with Helper
  with RequestHelper
  with Config
  with Secured
  with Loggable {

  def roster(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    withRosterContext(request, user, teamId) { rosterView: RosterView =>
      render {
        case Accepts.Html() => Ok(views.html.roster(rosterView.players, buildTeamView(teamId)))
        case Accepts.Json() => Ok(Json.toJson(rosterView))
      }
    }
  }
}
