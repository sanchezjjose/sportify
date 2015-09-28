package models


case class Season (
  _id: Long,
  title: String,
  game_ids: Set[Long],
  is_current: Boolean = false
)

object SeasonFields {
  val Id = "_id"
  val Title = "title"
  val GameIds = "game_ids"
  val IsCurrent = "is_current"
}
