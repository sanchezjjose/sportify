package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class ScheduleView(teams: TeamViewModel,
                       currentSeason: Option[Season],
                       games: List[Game],
                       nextGame: Option[Game])

object ScheduleView {

 implicit val writes: Writes[ScheduleView] = (
     (JsPath \ "teams").write[TeamViewModel] and
     (JsPath \ "current_season").write[Option[Season]] and
     (JsPath \ "games").write[List[Game]] and
     (JsPath \ "next_game").write[Option[Game]]
   )(unlift(ScheduleView.unapply))
}