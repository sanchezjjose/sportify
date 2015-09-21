package controllers

import javax.inject.Inject

import api.{SportifyDbApi, UserMongoDb}
import models.HomepageView
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import util.RequestHelper

import scala.concurrent.Future

class Homepage @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new SportifyDbApi(reactiveMongoApi)

  def index = isAuthenticatedAsync { implicit user => implicit request =>
    Future {
      Redirect(routes.Homepage.home(buildTeamView().current._id))
    }
  }

  def home(teamId: Long) = isAuthenticatedAsync { implicit user => implicit request =>
    withHomepageContext(request, user, teamId) { homepageView: HomepageView =>
      Ok(Json.toJson(homepageView))
    }
  }
}
