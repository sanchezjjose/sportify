package models


import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

import scala.collection.mutable.{Set => MSet}

case class HomepageView(teams: TeamViewModel,
                        nextGameInSeason: Option[Game],
                        playersIn: MSet[User],
                        playersOut: MSet[User])

object HomepageView {

  implicit val writes: Writes[HomepageView] = (
      (JsPath \ "teams").write[TeamViewModel] and
      (JsPath \ "next_game").write[Option[Game]] and
      (JsPath \ "players_in").write[MSet[User]] and
      (JsPath \ "players_out").write[MSet[User]]
    )(unlift(HomepageView.unapply))
}