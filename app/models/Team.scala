package models


case class TeamViewModel (
  selectedTeam: Team,
  otherTeams: Iterable[Team]
)

case class Team (
  _id: Long,
  name: String,
  player_ids: Set[Long],
  season_ids: Set[Long],
  sport: Sport,
  is_selected: Boolean = false
)

object TeamFields {
  val Id = "_id"
  val Name = "name"
  val PlayerIds = "player_ids"
  val SeasonIds = "season_ids"
  val Sport = "sport"
  val Selected = "selected"
}
