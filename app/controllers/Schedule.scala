package controllers

import models._
import play.api.libs.json._
import play.api.mvc._
import utils.{Helper, Loggable, RequestHelper}


object Schedule extends Controller
  with Helper
  with RequestHelper
  with Loggable
  with Secured {

  def schedule(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    withScheduleContext(request, user, teamId) { scheduleView: ScheduleView =>
      Ok(Json.toJson(scheduleView))
    }
  }
}
