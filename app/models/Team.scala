package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class TeamViewModel(
  current: Team,
  other: Iterable[Team]
)

object TeamViewModel {

  implicit val tvmWrites: Writes[TeamViewModel] = (
    (JsPath \ "current").write[Team] and
    (JsPath \ "other").write[Iterable[Team]]
  )(unlift(TeamViewModel.unapply))
}

case class Team(
  _id: Long,
  name: String,
  player_ids: Set[Long],
  season_ids: Set[Long],
  sport: Sport,
  selected: Boolean = false
) {

  def playersRequired: Int = {

    sport.name match { // TODO: use enums below
      case "Basketball" => 5
      case "Ping Pong" => 2
      case _ => sys.error("%s is not a supported sport at the moment".format(sport.name))
    }
  }
}

object Team {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val teamWrites: Writes[Team] = (
    (JsPath \ "_id").write[Long] and
      (JsPath \ "name").write[String] and
      (JsPath \ "player_ids").write[Set[Long]] and
      (JsPath \ "seasons").write[Set[Long]] and
      (JsPath \ "sport").write[Sport] and
      (JsPath \ "selected").write[Boolean]
    )(unlift(Team.unapply))
}

object TeamFields {
  val Id = "_id"
  val Name = "name"
  val PlayerIds = "player_ids"
  val SeasonIds = "season_ids"
  val Sport = "sport"
  val Selected = "selected"
}
