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
import java.util.UUID

case class Game(game_id: Long,
                game_seq: Int,
                startTime: String,
                address: String,
                gym: String,
                locationDetails: String,
                opponent: String,
                result: String,
                playerIds: Set[String] = Set.empty,
                is_playoff_game: Boolean,
                season: String)

object Game {

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def findNextGame: Iterator[Game] = {
    findAll.filter(g => DateTime.now().getMillis < format.parseDateTime(g.startTime).plusDays(1).getMillis)
  }

  def findByGameId(game_id: Long): Option[Game] = {
    val dbObject = MongoManager.gameCollection.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  /**
   * Retrieve all games.
   */
  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.gameCollection.find(MongoDBObject("season" -> "Spring 2013")).sort(MongoDBObject("game_id" -> 1))
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
                           game_seq = (game \ "game_seq").as[Int],
                           startTime = (game \ "start_time").as[String],
                           address = (game \ "address").as[String],
                           gym = (game \ "gym").as[String],
                           locationDetails = (game \ "location_details").as[String],
                           opponent = (game \ "opponent").as[String],
                           result = (game \ "result").as[String],
                           playerIds = (game \ "playerIds").as[Set[String]],
                           is_playoff_game = (game \ "is_playoff_game").as[Boolean],
                           season = (game \ "season").as[String])
                    }

    for(game <- gamesList) insert(game)
  }
}