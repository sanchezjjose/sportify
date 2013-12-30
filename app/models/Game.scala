package models

import scala.collection.mutable.Set
import controllers.MongoManager
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import io.Source
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime


case class GameForm(startTime: String,
                    address: String,
                    gym: String,
                    locationDetails: String,
                    opponent: String,
                    result: Option[String]) {

  def toGame: Game = {

    // Determine next game id and sequence
    val nextGame = Game.findLastGame.get
    val nextGameId = nextGame.game_id + 1
    val nextGameSeq = nextGame.game_seq + 1

    Game(nextGameId,
         nextGameSeq,
         startTime,
         address,
         gym,
         locationDetails,
         opponent,
         result = result.getOrElse(""),
         playersIn = Set.empty[String],
         playersOut = Set.empty[String],
         is_playoff_game = false,
         season = "Winter 2014")
  }

  def toGame(gameId: Int, gameSeq: Int): Game = {

    Game(gameId,
      gameSeq,
      startTime,
      address,
      gym,
      locationDetails,
      opponent,
      result = result.getOrElse(""),
      playersIn = Set.empty[String],
      playersOut = Set.empty[String],
      is_playoff_game = false,
      season = "Winter 2014")
  }
}

case class Game(game_id: Int,
                game_seq: Int,
                startTime: String,
                address: String,
                gym: String,
                locationDetails: String,
                opponent: String,
                result: String,
                playersIn: Set[String] = Set.empty,
                playersOut: Set[String] = Set.empty,
                is_playoff_game: Boolean,
                season: String)

object Game {

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def findNextGame: Option[Game] = {
    findAll.filter(g => DateTime.now().getMillis < format.parseDateTime(g.startTime).plusDays(1).getMillis).toList.headOption
  }

  def findLastGame: Option[Game] = {
    findAll.toIterable.lastOption
  }

  def findByGameId(game_id: Long): Option[Game] = {
    val dbObject = MongoManager.gamesColl.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.gamesColl.find(MongoDBObject("season" -> "Winter 2014")).sort(MongoDBObject("game_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findAllUpcomingGames: Iterator[Game] = {
    Game.findAll.filter(game => DateTime.now().getMillis < format.parseDateTime(game.startTime).getMillis)
  }

  def update(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gamesColl.update(MongoDBObject("game_id" -> game.game_id), dbo)
  }

  def updateScore(game_id: String, result: String, score: String) = {
    val gameOpt = findByGameId(game_id.toInt)

    gameOpt.map { game =>

      val resultUpdated = $set("result" -> "%s %s".format(result, score))
      MongoManager.gamesColl.update(MongoDBObject("game_id" -> game.game_id), resultUpdated)
    }
  }

  def removeGame(game_id: Long) {
    findByGameId(game_id).map { game =>
      MongoManager.gamesColl.remove(MongoDBObject("game_id" -> game_id))
    }
  }

	/**
   * Insert a new game.
   *
   * @param game The user values
   */
  def insert(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gamesColl += dbo
  }


  //TODO: convert this to endpoint -- /games/load/<resource_location>
  def loadGames : Unit = {
    val jsonString = Source.fromFile("app/resources/games.json")
    val json: JsValue = Json.parse(jsonString mkString)

    val games = (json \ "games").as[List[JsObject]]

    val gamesList = games.map { game =>
                      Game(game_id = (game \ "game_id").as[Int],
                           game_seq = (game \ "game_seq").as[Int],
                           startTime = (game \ "start_time").as[String],
                           address = (game \ "address").as[String],
                           gym = (game \ "gym").as[String],
                           locationDetails = (game \ "location_details").as[String],
                           opponent = (game \ "opponent").as[String],
                           result = (game \ "result").as[String],
                           playersIn = (game \ "playersIn").as[Set[String]],
                           playersOut = (game \ "playersOut").as[Set[String]],
                           is_playoff_game = (game \ "is_playoff_game").as[Boolean],
                           season = (game \ "season").as[String])
                    }

    for(game <- gamesList) insert(game)
  }
}