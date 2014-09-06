package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.{Loggable, MongoManager}
import models.CustomPlaySalatContext._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.mutable.Set



case class Game (_id: Long,
                 number: Int,
                 start_time: String,
                 address: String,
                 gym: String,
                 location_details: Option[String],
                 opponent: String,
                 result: Option[String],
                 players_in: Set[Player] = Set.empty,
                 players_out: Set[Player] = Set.empty,
                 is_playoff_game: Boolean = false)

object Game {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val gameWrites: Writes[Game] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "number").write[Int] and
    (JsPath \ "start_time").write[String] and
    (JsPath \ "address").write[String] and
    (JsPath \ "gym").write[String] and
    (JsPath \ "location_details").write[Option[String]] and
    (JsPath \ "opponent").write[String] and
    (JsPath \ "result").write[Option[String]] and
    (JsPath \ "players_in").write[Set[Player]] and
    (JsPath \ "players_out").write[Set[Player]] and
    (JsPath \ "is_playoff_game").write[Boolean]
  )(unlift(Game.unapply))

  val format = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def findNextGame: Option[Game] = {
    findAllInCurrentSeason.filter(g => DateTime.now().getMillis < format.parseDateTime(g.start_time).plusDays(1).getMillis).toList.headOption
  }

  def findLastGame: Option[Game] = {
    findAll.toIterable.lastOption
  }

  def findLastGameInCurrentSeason: Option[Game] = {
    findAllInCurrentSeason.toIterable.lastOption
  }

  def findById(id: Long): Option[Game] = {
    val dbObject = MongoManager.games.findOne(MongoDBObject("_id" -> id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.games.find().sort(MongoDBObject("_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findAllInCurrentSeason: Iterator[Game] = {
    val currentSeason = Season.findCurrentSeason()
    val dbObjects = MongoManager.games.find(MongoDBObject("season._id" -> currentSeason.get._id)).sort(MongoDBObject("_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findAllUpcomingGames: Iterator[Game] = {
    Game.findAllInCurrentSeason.filter(game => DateTime.now().getMillis < format.parseDateTime(game.start_time).getMillis)
  }

  def update(game: Game): Unit = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.games.update(MongoDBObject("_id" -> game._id), dbo)
  }

  def updateScore(game_id: Long, result: String, score: String): Unit = {
    findById(game_id.toInt).map { game =>
      val resultUpdated = $set("result" -> "%s %s".format(result, score))
      MongoManager.games.update(MongoDBObject("_id" -> game._id), resultUpdated)
    }
  }

  def removeGame(id: Long): Unit = {
    findById(id).map { game =>
      MongoManager.games.remove(MongoDBObject("_id" -> id))
    }
  }

  def create(game: Game) = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.games += dbo
  }
}

case class GameForm(startTime: String,
                    address: String,
                    gym: String,
                    locationDetails: Option[String],
                    opponent: String,
                    result: Option[String]) extends Loggable with Helper {

  /**
   * Create new game from the add game form.
   */
  def toNewGame(isPlayoffGame: Boolean): Game = {

    // Determine new game number
    val number = Game.findLastGameInCurrentSeason.map( _.number + 1 ).getOrElse(1)

    Game(_id = generateRandomId(),
         number = number,
         start_time = startTime,
         address = address,
         gym = gym,
         location_details = locationDetails,
         opponent = opponent,
         result = result,
         players_in = Set.empty[Player],
         players_out = Set.empty[Player],
         is_playoff_game = isPlayoffGame)
  }

  /**
   * Update existing game with values from the edit game form.
   */
  def toGame(id: Long, number: Int, isPlayoffGame: Boolean): Game = {

    Game.findById(id).map { game =>
      Game(_id = id,
           number = number,
           start_time = startTime,
           address = address,
           gym = gym,
           location_details = locationDetails,
           opponent = opponent,
           result = result,
           players_in = game.players_in,
           players_out = game.players_out,
           is_playoff_game = isPlayoffGame)
    }.get
  }
}