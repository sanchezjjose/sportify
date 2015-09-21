package api

import scala.concurrent.{ ExecutionContext, Future }

import play.api.Play.current
import play.api.libs.json.{ JsObject, Json }

import reactivemongo.bson.BSONDocument
import reactivemongo.api.commands.WriteResult

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

trait PostRepo {

  def find()(implicit ec: ExecutionContext): Future[List[JsObject]]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PostMongoRepo(reactiveMongoApi: ReactiveMongoApi) extends PostRepo {
  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection =
    reactiveMongoApi.db.collection[JSONCollection]("posts")

  def find()(implicit ec: ExecutionContext): Future[List[JsObject]] =
    collection.find(Json.obj()).cursor[JsObject].collect[List]()

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update)

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.remove(document)

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.save(document)
}
