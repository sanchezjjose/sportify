package models

import scala.collection.mutable.Set
import controllers.MongoManager
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._

case class Game(id: Long,
                startTime: String,
                address: String,
                gym: String,
                opponent: String,
                result: String,
                playerIds: Set[ObjectId] = Set.empty)

object Game {

  def findByGameId(id: Long): Option[Game] = {
    val dbObject = MongoManager.gameCollection.findOne( MongoDBObject("id" -> id) )
    dbObject.map(o => grater[Game].asObject(o))
  }

  /**
   * Retrieve all games.
   */
  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.gameCollection.find()
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def update(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gameCollection.update(MongoDBObject("id" -> game.id), dbo)
  }

	/**
   * Insert a new game.
   *
   * @param game The user values
   */
  def insert(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gameCollection += dbo
  }
}