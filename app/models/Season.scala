package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.{MongoManagerFactory, MongoManager}
import models.CustomPlaySalatContext._
import scala.collection.mutable.{Set => MSet}

/**
 * Model for a season. This can hold any value that represents a season for a sport.
 *
 * i.e.,
 * Basketball might have 'Summer 2014', 'Fall 2014', 'Winter 2014-2015'.
 * Softball on the other hand might only have '2014' as the season name.
 *
 */
case class Season (_id: Long,
                   title: String,
                   game_ids: MSet[Long],
                   is_current_season: Boolean)

object Season {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val seasonWrites: Writes[Season] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "title").write[String] and
    (JsPath \ "game_ids").write[MSet[Long]] and
    (JsPath \ "is_current_season").write[Boolean]
  )(unlift(Season.unapply))


  /*
   * MONGO API -- TODO: move to separate DB Trait
   */

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