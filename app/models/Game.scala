package models

import scala.collection.mutable.Set
import controllers.{Config, Loggable, MongoManager}
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.mongodb.casbah.Imports._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import CustomPlaySalatContext._

case class GameForm(startTime: String,
                    address: String,
                    gym: String,
                    locationDetails: String,
                    opponent: String,
                    result: Option[String]) extends Loggable {

  /**
   * Create new game from the add game form.
   */
  def toNewGame(isPlayoffGame: Boolean): Game = {

    // Determine next game id and sequence
    val nextGameId = Game.findLastGame.map( _.game_id + 1).getOrElse(1)
    val nextGameSeq = Game.findLastGameInCurrentSeason.map( _.game_seq + 1).getOrElse(1)

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
         is_playoff_game = isPlayoffGame,
         season = Config.season)
  }

  /**
   * Update existing game with values from the edit game form.
   */
  def toGame(gameId: Int, gameSeq: Int, isPlayoffGame: Boolean): Game = {

    Game.findByGameId(gameId).map { game =>
      Game(gameId,
        gameSeq,
        startTime,
        address,
        gym,
        locationDetails,
        opponent,
        result = result.getOrElse(""),
        playersIn = game.playersIn,
        playersOut = game.playersOut,
        is_playoff_game = isPlayoffGame,
        season = Config.season)
    }.get
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
    findAllInCurrentSeason.filter(g => DateTime.now().getMillis < format.parseDateTime(g.startTime).plusDays(1).getMillis).toList.headOption
  }

  def findLastGame: Option[Game] = {
    findAll.toIterable.lastOption
  }

  def findLastGameInCurrentSeason: Option[Game] = {
    findAllInCurrentSeason.toIterable.lastOption
  }

  def findByGameId(game_id: Long): Option[Game] = {
    val dbObject = MongoManager.gamesColl.findOne(MongoDBObject("game_id" -> game_id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.gamesColl.find().sort(MongoDBObject("game_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findAllInCurrentSeason: Iterator[Game] = {
    val dbObjects = MongoManager.gamesColl.find(MongoDBObject("season" -> Config.season)).sort(MongoDBObject("game_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findAllUpcomingGames: Iterator[Game] = {
    Game.findAllInCurrentSeason.filter(game => DateTime.now().getMillis < format.parseDateTime(game.startTime).getMillis)
  }

  def update(game: Game): Unit = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.gamesColl.update(MongoDBObject("game_id" -> game.game_id), dbo)
  }

  def updateScore(game_id: String, result: String, score: String): Unit = {
    findByGameId(game_id.toInt).map { game =>
      val resultUpdated = $set("result" -> "%s %s".format(result, score))
      MongoManager.gamesColl.update(MongoDBObject("game_id" -> game.game_id), resultUpdated)
    }
  }

  def removeGame(game_id: Long): Unit = {
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
}