package controllers

import javax.inject.Inject
import api.UserMongoDb
import models.RosterView
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper


class Roster @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new UserMongoDb(reactiveMongoApi)

  def roster(teamId: Long) = isAuthenticatedAsync { user => implicit request =>
    withRosterContext(request, user, teamId) { rosterView: RosterView =>
      Ok(Json.toJson(rosterView))
    }
  }
}
