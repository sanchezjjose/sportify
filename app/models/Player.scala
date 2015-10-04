package models


case class Player (
  _id: Long,
  user_id: Long,
  team_id: Long,
  number: Int,
  position: Option[String]
)

object PlayerFields {
  val Id = "_id"
  val UserId = "user_id"
  val TeamId = "team_id"
  val Number = "number"
  val Position = "position"
}
