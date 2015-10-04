package controllers

import javax.inject.Inject

import api.MongoManager
import models.JsonFormats._
import models.RosterViewModel
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper


class Roster @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new MongoManager(reactiveMongoApi)

  def roster(teamId: Long) = isAuthenticatedAsync { userContextFuture => implicit request =>
    withRosterContext(request, userContextFuture, teamId) { rosterView: RosterViewModel =>
      Ok(Json.toJson(rosterView))
    }
  }
}
