package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import java.util.Date
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
                players: Set[User])

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