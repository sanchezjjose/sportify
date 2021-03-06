package models


case class Season (
  _id: Long,
  name: String,
  team_ids: Set[Long],
  game_ids: Set[Long],
  is_active: Boolean = false
)

object SeasonFields {
  val Id = "_id"
  val Name = "name"
  val TeamIds = "team_ids"
  val GameIds = "game_ids"
  val IsActive = "is_active"
}
