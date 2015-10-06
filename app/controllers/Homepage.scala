package controllers

import javax.inject.Inject
import api.MongoManager
import models.HomepageViewModel
import models.JsonFormats._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper
import scala.concurrent.ExecutionContext.Implicits.global


class Homepage @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val mongoDb = new MongoManager(reactiveMongoApi)

  def index = isAuthenticatedAsync { implicit userContextFuture => implicit request =>
    userContextFuture.map { userContext =>
      Redirect(routes.Homepage.home(userContext.teams.head._id))
    }
  }

  def home(teamId: Long) = isAuthenticatedAsync { implicit userContextFuture => implicit request =>
    withHomepageContext(request, userContextFuture, teamId) { homepageView: HomepageViewModel =>
      Ok(Json.toJson(homepageView))
    }
  }
}
