package api

import models.Team
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

trait TeamDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Team]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Team]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class TeamMongoDb(reactiveMongoApi: ReactiveMongoApi) extends TeamDb {

  // BSON-JSON conversions

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("teams")

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Team]] = ???

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Team]] = ???

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???
}

/*

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

 */