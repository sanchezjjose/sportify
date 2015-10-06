package controllers

import javax.inject.Inject

import api.MongoManager
import models.JsonFormats._
import models.ScheduleViewModel
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper


class Schedule @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val mongoDb = new MongoManager(reactiveMongoApi)

  def schedule(teamId: Long) = isAuthenticatedAsync { userContext => implicit request =>
    withScheduleContext(request, userContext, teamId) { scheduleView: ScheduleViewModel =>
      Ok(Json.toJson(scheduleView))
    }
  }
}
