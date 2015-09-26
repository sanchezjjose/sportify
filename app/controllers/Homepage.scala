package controllers

import javax.inject.Inject
import api.SportifyDbApi
import models.HomepageView
import models.JsonFormats._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper
import scala.concurrent.ExecutionContext.Implicits.global


class Homepage @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new SportifyDbApi(reactiveMongoApi)

  def index = isAuthenticatedAsync { implicit user => implicit request =>
    buildTeamView().map { tVm =>
      Redirect(routes.Homepage.home(tVm.current._id))
    }
  }

  def home(teamId: Long) = isAuthenticatedAsync { implicit user => implicit request =>
    withHomepageContext(request, user, teamId) { homepageView: HomepageView =>
      Ok(Json.toJson(homepageView))
    }
  }
}
