package controllers

import com.sportify.config.Config
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.{Loggable, Helper}

object Roster extends Controller
  with Helper
  with Config
  with Secured
  with Loggable {


  def roster(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView(teamId)
    val pVm = buildPlayerViews(teamId).toList.sortBy(p => p.name)


    Ok(views.html.roster(pVm, tVm))

//    render {
//      case Accepts.Html() => Ok(views.html.roster(pVm, tVm))
//      case Accepts.Json() => Ok(Json.toJson(pVm))
//    }
  }

//  def rosterApi(teamId: Long) = Action { implicit request =>
//    val tVm = buildTeamView(teamId)
//    val pVm = buildPlayerViews(teamId).toList.sortBy(p => p.name)
//
//    Ok(Json.toJson(tVm, pVm))
//  }
}
