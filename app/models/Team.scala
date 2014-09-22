package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.MongoManager
import models.CustomPlaySalatContext._
import scala.collection.mutable.{Set => MSet}

/**
 * Model of a team, which is made up of players and a specific sport.
 */
case class Team (_id: Long,
                 name: String,
                 players: MSet[Player],
                 season_ids: Set[Long],
                 sport: Sport) {

  def playersNeeded: Int = {
  // import Sport.Name._
  // TODO: use enums below

    sport.name match {
      case "Basketball" => 5
      case "Ping Pong" => 2
      case _ => sys.error("%s is not a supported sport at the moment".format(sport.name))
    }
  }
}

object Team {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val teamWrites: Writes[Team] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "players").write[MSet[Player]] and
    (JsPath \ "seasons").write[Set[Long]] and
    (JsPath \ "sport").write[Sport]
  )(unlift(Team.unapply))


  def findById(id: Long): Option[Team] = {
    val dbObject = MongoManager.teams.findOne( MongoDBObject("_id" -> id) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def findByName(name: String): Option[Team] = {
    val dbObject = MongoManager.teams.findOne( MongoDBObject("name" -> name) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def findAll: Set[Team] = {
    val dbObjects = MongoManager.teams.find().sort(MongoDBObject("_id" -> 1))
    (for (x <- dbObjects) yield grater[Team].asObject(x)).toSet[Team]
  }

  def update(team: Team): Unit = {
    val dbo = grater[Team].asDBObject(team)
    MongoManager.teams.update(MongoDBObject("_id" -> team._id), dbo)
  }

}