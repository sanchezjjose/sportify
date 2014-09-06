package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.MongoManager
import models.CustomPlaySalatContext._
import scala.collection.mutable.Set

/**
 * Model of a team, which is made up of players and a specific sport.
 */
case class Team (_id: Long,
                 name: String,
                 players: Set[Player],
                 sport: Sport)

object Team {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val teamWrites: Writes[Team] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "players").write[Set[Player]] and
    (JsPath \ "sport").write[Sport]
  )(unlift(Team.unapply))


  def findById(id: Long): Option[Team] = {
    val dbObject = MongoManager.teams.findOne( MongoDBObject("_id" -> id) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def update(team: Team): Unit = {
    val dbo = grater[Team].asDBObject(team)
    MongoManager.teams.update(MongoDBObject("_id" -> team._id), dbo)
  }

}