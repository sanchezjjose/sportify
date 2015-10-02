package api

import models.Season
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}


trait SeasonDao {

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Season]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Season]]

  def insert(season: Season)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class SeasonMongoDao(reactiveMongoApi: ReactiveMongoApi) extends SeasonDao {

  // BSON-JSON conversions
  import models.JsonFormats._
  import play.modules.reactivemongo.json._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("seasons")

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Season]] = {
    collection.find(query).one[Season]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Season]] = {
    collection.find(query).cursor[Season].collect[List]()
  }

  override def insert(season: Season)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(season)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
