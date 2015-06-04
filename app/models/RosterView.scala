package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class RosterView(selectedTeamId: Long, players: List[PlayerViewModel])

object RosterView {

  implicit val writes: Writes[RosterView] = (
      (JsPath \ "selected_team_id").write[Long] and
      (JsPath \ "players").write[List[PlayerViewModel]]
    )(unlift(RosterView.unapply))
}
