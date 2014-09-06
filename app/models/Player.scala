package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.MongoManager
import models.User._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


/**
 * A player is a registered user who is a part of a team.
 */
case class Player(_id: Long,
                  number: Int,
                  position: String)


object Player {

  implicit val playerWrites: Writes[Player] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "number").write[Int] and
    (JsPath \ "position").write[String]
  )(unlift(Player.unapply))


//  def findById(id: Long): Option[Player] = {
//    val dbObject = MongoManager.players.findOne( MongoDBObject("_id" -> id) )
//    dbObject.map(o => grater[Player].asObject(o))
//  }
//
//  def findAll: Iterator[Player] = {
//    val dbObjects = MongoManager.players.find()
//    for (x <- dbObjects) yield grater[Player].asObject(x)
//  }
//
//  def create(player: Player) = {
//    val dbo = grater[Player].asDBObject(player)
//    MongoManager.players += dbo
//  }
}