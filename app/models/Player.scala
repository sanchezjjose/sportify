package models


case class Player (
  _id: Long,
  number: Int,
  position: Option[String]
)

object PlayerFields {
  val Id = "_id"
  val Number = "number"
  val Position = "position"
}
