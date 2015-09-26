package models


case class TeamViewModel (
  current: Team,
  other: Iterable[Team]
)

case class Team (
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

object TeamFields {
  val Id = "_id"
  val Name = "name"
  val PlayerIds = "player_ids"
  val SeasonIds = "season_ids"
  val Sport = "sport"
  val Selected = "selected"
}
