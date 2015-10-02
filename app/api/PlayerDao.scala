package api

import models.Player
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}


trait PlayerDao {

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Player]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Player]]

  def save(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PlayerMongoDao(reactiveMongoApi: ReactiveMongoApi) extends PlayerDao {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import models.JsonFormats._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("players")

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Player]] = {
    collection.find(query).one[Player]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Player]] = {
    collection.find(query).cursor[Player].collect[List]()
  }

  override def save(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.save(document)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
