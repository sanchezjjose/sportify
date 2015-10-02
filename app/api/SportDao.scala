package api

import models.Sport
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}


trait SportDao {

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Sport]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Sport]]

  def insert(sport: Sport)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class SportMongoDao(reactiveMongoApi: ReactiveMongoApi) extends SportDao {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import models.JsonFormats._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("sports")

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Sport]] = {
    collection.find(query).one[Sport]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Sport]] = {
    collection.find(query).cursor[Sport].collect[List]()
  }

  override def insert(sport: Sport)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(sport)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
