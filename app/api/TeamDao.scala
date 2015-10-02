package api

import models.Team
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContext, Future}

trait TeamDao {

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Team]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Team]]

  def insert(team: Team)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class TeamMongoDao(reactiveMongoApi: ReactiveMongoApi) extends TeamDao {

  // BSON-JSON conversions
  import models.JsonFormats._
  import play.modules.reactivemongo.json._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("teams")

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Team]] = {
    collection.find(query).one[Team]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Team]] = {
    collection.find(query).cursor[Team].collect[List]()
  }

  override def insert(team: Team)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(team)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
