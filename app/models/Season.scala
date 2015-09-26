package models


case class Season (
  _id: Long,
  title: String,
  game_ids: Set[Long],
  is_current_season: Boolean
)

object SeasonFields {
  val Id = "_id"
  val Title = "title"
  val GameIds = "game_ids"
  val IsCurrentSeason = "is_current_season"
}
