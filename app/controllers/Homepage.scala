package controllers

import models._
import play.api.libs.json.Json
import play.api.mvc.Controller
import utils.{Helper, RequestHelper}

object Homepage extends Controller
  with Secured
  with Helper
  with RequestHelper {

  def index = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView
    Redirect(routes.Homepage.home(tVm.current._id))
  }

  def home(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    withHomepageContext(request, user, teamId) { homepageView: HomepageView =>
      Ok(Json.toJson(homepageView))
    }
  }
}
