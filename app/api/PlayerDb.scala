package api

import models.Player
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}


trait PlayerDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Player]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Player]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PlayerMongoDb(reactiveMongoApi: ReactiveMongoApi) extends PlayerDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("players")

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Player]] = {
    collection.find(query).one[Player]
  }

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Player]] = {
    collection.find(query).cursor[Player].collect[List]()
  }

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.save(document)
  }

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
