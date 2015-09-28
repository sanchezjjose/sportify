package api

import models.Sport
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}


trait SportDao {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Sport]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Sport]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class SportMongoDao(reactiveMongoApi: ReactiveMongoApi) extends SportDao {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import ImplicitBSONHandlers._
  import models.JsonFormats._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("sports")

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Sport]] = {
    collection.find(query).one[Sport]
  }

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Sport]] = {
    collection.find(query).cursor[Sport].collect[List]()
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
