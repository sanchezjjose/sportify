package models



 case class Season (_id: Long,
                    title: String,
                    game_ids: Set[Long],
                    is_current_season: Boolean)

object Season {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val seasonWrites: Writes[Season] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "title").write[String] and
    (JsPath \ "game_ids").write[Set[Long]] and
    (JsPath \ "is_current_season").write[Boolean]
  )(unlift(Season.unapply))



  private val mongoManager = MongoManagerFactory.instance

  def findById(id: Long): Option[Season] = {
    val dbObject = mongoManager.seasons.findOne(MongoDBObject("_id" -> id))
    dbObject.map(o => grater[Season].asObject(o))
  }

  def findLastGameIdInSeason(seasonId: Long): Option[Long] = {
    findById(seasonId).get.game_ids.toIterable.lastOption
  }

  def update(season: Season): Unit = {
    val dbo = grater[Season].asDBObject(season)
    mongoManager.seasons.update(MongoDBObject("_id" -> season._id), dbo)
  }
}