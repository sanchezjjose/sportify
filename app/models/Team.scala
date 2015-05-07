package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.{MongoManagerFactory, MongoManager}
import models.CustomPlaySalatContext._
import scala.collection.mutable.{Set => MSet}


case class TeamViewModel(current: Team, other: Iterable[Team])

/**
 * Model of a team, which is made up of players and a specific sport.
 */
case class Team (_id: Long,
                 name: String,
                 player_ids: MSet[Long],
                 season_ids: Set[Long],
                 sport: Sport) {

  def playersRequired: Int = {

    sport.name match { // TODO: use enums below
      case "Basketball" => 5
      case "Ping Pong" => 2
      case _ => sys.error("%s is not a supported sport at the moment".format(sport.name))
    }
  }
}

object Team {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val teamWrites: Writes[Team] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "name").write[String] and
    (JsPath \ "player_ids").write[MSet[Long]] and
    (JsPath \ "seasons").write[Set[Long]] and
    (JsPath \ "sport").write[Sport]
  )(unlift(Team.unapply))


  def getNextGameByTeam: Set[(Team, Game)] = {
    for (team <- Team.findAll;
         seasonId <- team.season_ids.filter(Season.findById(_).exists(_.is_current_season));
         currentSeason <- Season.findById(seasonId);
         nextGame <- Game.getNextGame(currentSeason.game_ids)) yield (team, nextGame)
  }


  /*
   * MONGO API -- TODO: move to separate DB Trait
   */

  private val mongoManager = MongoManagerFactory.instance

  def findById(id: Long): Option[Team] = {
    val dbObject = mongoManager.teams.findOne( MongoDBObject("_id" -> id) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def findByName(name: String): Option[Team] = {
    val dbObject = mongoManager.teams.findOne( MongoDBObject("name" -> name) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def findAll: Set[Team] = {
    val dbObjects = mongoManager.teams.find().sort(MongoDBObject("_id" -> 1))
    (for (x <- dbObjects) yield grater[Team].asObject(x)).toSet[Team]
  }

  def findAllByPlayerId(playerId: Long): Iterator[Team] = {
    val dbObject = mongoManager.teams.find( MongoDBObject("player_ids" -> playerId) )
    dbObject.map(o => grater[Team].asObject(o))
  }

  def findAllByUser(user: User): Set[Team] = {
    user.players.flatMap { player =>
      val dbObject = mongoManager.teams.find( MongoDBObject("player_ids" -> player.id) )
      dbObject.map(o => grater[Team].asObject(o))
    }.toSet
  }

  def update(team: Team): Unit = {
    val dbo = grater[Team].asDBObject(team)
    mongoManager.teams.update(MongoDBObject("_id" -> team._id), dbo)
  }

}