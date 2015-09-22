package api

import models.Season
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}


trait SeasonDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Season]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Season]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class SeasonMongoDb(reactiveMongoApi: ReactiveMongoApi) extends SeasonDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("seasons")

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Season]] = ???

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Season]] = ???

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???
}

/*

def findById(id: Long): Option[Season] = {
    val dbObject = mongoManager.seasons.findOne(MongoDBObject("_id" -> id))
    dbObject.map(o => grater[Season].asObject(o))
  }

  def findLastGameIdInSeason(seasonId: Long): Option[Long] = {
    findById(seasonId).get.game_ids.toIterable.lastOption
  }

  def update(season: Season): Unit = {
    val dbo = grater[Season].asDBObject(season)
    mongoManager.seasons.update(MongoDBObject("_id" -> season._id), dbo)
  }

 */