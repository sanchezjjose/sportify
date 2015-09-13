package models


import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class HomepageView(teams: TeamViewModel,
                        nextGameInSeason: Option[Game],
                        playersIn: Set[User],
                        playersOut: Set[User])

object HomepageView {

  implicit val writes: Writes[HomepageView] = (
      (JsPath \ "teams").write[TeamViewModel] and
      (JsPath \ "next_game").write[Option[Game]] and
      (JsPath \ "players_in").write[Set[User]] and
      (JsPath \ "players_out").write[Set[User]]
    )(unlift(HomepageView.unapply))
}