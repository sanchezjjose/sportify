package controllers

import javax.inject.Inject

import api.UserMongoDb
import models.ScheduleView
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper

class Schedule @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new UserMongoDb(reactiveMongoApi)

  def schedule(teamId: Long) = isAuthenticatedAsync { user => implicit request =>
    withScheduleContext(request, user, teamId) { scheduleView: ScheduleView =>
      Ok(Json.toJson(scheduleView))
    }
  }
}
