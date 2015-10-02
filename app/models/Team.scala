package models


case class Team (
  _id: Long,
  name: String,
  player_ids: Set[Long],
  sport: Sport
)

object TeamFields {
  val Id = "_id"
  val Name = "name"
  val PlayerIds = "player_ids"
  val Sport = "sport"
}
