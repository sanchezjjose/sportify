package models


case class Game (_id: Long, // TODO: change from Long to Int for all '_id' variables
                number: Int,
                start_time: Long,
                address: String,
                gym: String,
                location_details: Option[String],
                opponent: String,
                result: Option[String],
                players_in: Set[Long] = Set.empty[Long], // corresponds to player id's
                players_out: Set[Long] = Set.empty[Long], // corresponds to player id's
                is_playoff_game: Boolean = false)

object Game {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val gameWrites: Writes[Game] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "number").write[Int] and
    (JsPath \ "start_time").write[Long] and
    (JsPath \ "address").write[String] and
    (JsPath \ "gym").write[String] and
    (JsPath \ "location_details").write[Option[String]] and
    (JsPath \ "opponent").write[String] and
    (JsPath \ "result").write[Option[String]] and
    (JsPath \ "players_in").write[Set[Long]] and
    (JsPath \ "players_out").write[Set[Long]] and
    (JsPath \ "is_playoff_game").write[Boolean]
  )(unlift(Game.unapply))
}

object GameFields {
  val Id = "_id"
  val Number = "number"
  val StartTime = "start_time"
  val Address = "address"
  val Gym = "gym"
  val LocationDetails = "location_details"
  val Opponent = "opponent"
  val Result = "result"
  val PlayersIn = "players_in"
  val PlayersOut = "players_out"
  val IsPlayoffGame = "is_playoff_game"
}
