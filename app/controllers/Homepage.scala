package controllers

import javax.inject.Inject
import api.MongoManager
import models.HomepageViewModel
import models.JsonFormats._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper
import scala.concurrent.Future


class Homepage @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new MongoManager(reactiveMongoApi)

  def index = isAuthenticatedAsync { implicit userContext => implicit request =>
    Future successful Redirect(routes.Homepage.home(userContext.selectedTeam._id))
  }

  def home(teamId: Long) = isAuthenticatedAsync { implicit userContext => implicit request =>
    withHomepageContext(request, userContext, teamId) { homepageView: HomepageViewModel =>
      Ok(Json.toJson(homepageView))
    }
  }
}
