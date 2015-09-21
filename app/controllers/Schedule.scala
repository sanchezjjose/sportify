package controllers

import models.ScheduleView
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{Helper, RequestHelper}

object Schedule extends Controller
  with Helper
  with RequestHelper
  with Secured {

  def schedule(teamId: Long) = Action { /*implicit user =>*/ implicit request =>
    withScheduleContext(request, user, teamId) { scheduleView: ScheduleView =>
      Ok(Json.toJson(scheduleView))
    }
  }
}
