package models

import scala.collection.mutable.Set
import controllers.MongoManager
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import io.Source
import play.api.libs.json._
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.DateTime

case class Game(game_id: Long,
                startTime: String,
                address: String,
                gym: String,
                locationDetails: String,
                opponent: String,
                result: String,
                playerIds: Set[ObjectId] = Set.empty)

object Game {

  def findNextGame(): Unit = {
    val fmt = DateTimeFormat.forPattern("E MM/dd, HH:mm a")
    val dt = fmt.parseDateTime("Thu 03/07, 07:00 PM")
    println(dt.dayOfWeek().get())
  }

  def findByGameId(game_id: Long): Option[Game] = {
    val dbObject = MongoManager.gameCollection.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  /**
   * Retrieve all games.
   */
  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.gameCollection.find().sort(MongoDBObject("game_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def update(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gameCollection.update(MongoDBObject("game_id" -> game.game_id), dbo)
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

  def loadGames : Unit = {
    val jsonString = Source.fromFile("app/resources/games.json")
    val json: JsValue = Json.parse(jsonString mkString)

    val games = (json \ "games").as[List[JsObject]]

    val gamesList = games.map { game =>
                      Game(game_id = (game \ "game_id").as[Long],
                           startTime = (game \ "start_time").as[String],
                           address = (game \ "address").as[String],
                           gym = (game \ "gym").as[String],
                           locationDetails = (game \ "location_details").as[String],
                           opponent = (game \ "opponent").as[String],
                           result = (game \ "result").as[String])
                    }

    for(game <- gamesList) insert(game)
  }
}