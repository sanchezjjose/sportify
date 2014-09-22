package controllers

import models.{User, Team}
import play.api.mvc._


trait Teams {

  private[controllers] def getSelectedTeam(request: Request[AnyContent]): Team = {
    (for (name <- request.cookies.get("team_name");
          team <- Team.findByName(name.value)) yield team).getOrElse(Team.findAll.head)
  }

  private[controllers] def getOtherTeams(request: Request[AnyContent]): Set[Team] = {
    Team.findAll.filter(team =>
      team.name != getSelectedTeam(request).name &&
      team.players.contains(User.loggedInUser.player.get) // TODO: need better way to check for this
    )
  }
}

object Teams extends Controller {

  def switch(team_name: String) = Action { implicit request =>
    Redirect(routes.Application.home()).withCookies(Cookie("team_name", team_name))
  }
}