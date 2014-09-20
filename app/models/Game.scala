package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import models.CustomPlaySalatContext._
import controllers.{MongoManager, Loggable}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.mutable.Set


case class Game (_id: Long, // TODO: change from Long to Int for all '_id' variables
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


  val gameDateFormat = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

  def findById(id: Long): Option[Game] = {
    val dbObject = MongoManager.games.findOne(MongoDBObject("_id" -> id))
    dbObject.map(o => grater[Game].asObject(o))
  }

  def findNextGame: Option[Game] = {
    findAll.filter(g =>
      // TODO: replace plusDays(1) with another solution, to allow for back to back games, etc...
      DateTime.now().getMillis < gameDateFormat.parseDateTime(g.start_time).plusDays(1).getMillis
    ).toList
      .sortBy(g => gameDateFormat.parseDateTime(g.start_time).plusDays(1).getMillis)
        .headOption
  }

  def findAll: Iterator[Game] = {
    val dbObjects = MongoManager.games.find().sort(MongoDBObject("_id" -> 1))
    for (x <- dbObjects) yield grater[Game].asObject(x)
  }

  def findLastGame: Option[Game] = {
    findAll.toIterable.lastOption
  }

  def findAllUpcomingGames: Iterator[Game] = {
    findAll.filter { game =>
      DateTime.now().getMillis < gameDateFormat.parseDateTime(game.start_time).getMillis
    }.toIterator
  }

  def create(game: Game): Unit = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.games += dbo
  }

  def remove(id: Long): Unit = {
    MongoManager.games.remove(MongoDBObject("_id" -> id))
  }

  def update(game: Game): Unit = {
    val dbo = grater[Game].asDBObject(game)
    MongoManager.games.update(MongoDBObject("_id" -> game._id), dbo)
  }
}

case class GameForm(number: Option[String],
                    startTime: String,
                    address: String,
                    gym: String,
                    locationDetails: Option[String],
                    opponent: String,
                    result: Option[String]) extends Loggable with Helper {

  /**
   * Create new game from the add game form.
   */
  def toNewGame(seasonId: Long, isPlayoffGame: Boolean): Game = {

    val gameNumberOpt = for (gameId <- Season.findLastGameIdInSeason(seasonId);
                             game <- Game.findById(gameId)) yield game.number

    Game(_id = generateRandomId(),
         number = gameNumberOpt.getOrElse(0) + 1, // Determine game number
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
   * Create a new game from existing game with values from the edit game form.
   */
  def toGame(gameId: Long, isPlayoffGame: Boolean): Game = {
    Game.findById(gameId).get
      .copy(number = number.get.toInt,
            start_time = startTime,
            address = address,
            gym = gym,
            opponent = opponent,
            location_details = locationDetails,
            result = result)
  }
}