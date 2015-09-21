package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class RosterView(teams: TeamViewModel,
                     players: List[PlayerViewModel])

object RosterView {

 implicit val writes: Writes[RosterView] = (
     (JsPath \ "teams").write[TeamViewModel] and
     (JsPath \ "players").write[List[PlayerViewModel]]
   )(unlift(RosterView.unapply))
}
