package models


case class Season (
  _id: Long,
  title: String,
  game_ids: Set[Long],
  is_current_season: Boolean
)

//object Season {
//
//  import play.api.libs.functional.syntax._
//  import play.api.libs.json._
//
//  implicit val seasonWrites: Writes[Season] = (
//    (JsPath \ "_id").write[Long] and
//    (JsPath \ "title").write[String] and
//    (JsPath \ "game_ids").write[Set[Long]] and
//    (JsPath \ "is_current_season").write[Boolean]
//  )(unlift(Season.unapply))
//}

object SeasonFields {
  val Id = "_id"
  val Title = "title"
  val GameIds = "game_ids"
  val IsCurrentSeason = "is_current_season"
}
