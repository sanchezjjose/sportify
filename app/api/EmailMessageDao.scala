package api

import models.EmailMessage
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{Future, ExecutionContext}

trait EmailMessageDao {

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[EmailMessage]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[EmailMessage]]

  def insert(emailMessage: EmailMessage)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class EmailMessageMongoDao(reactiveMongoApi: ReactiveMongoApi) extends EmailMessageDao {

  // BSON-JSON conversions
  import models.JsonFormats._
  import play.modules.reactivemongo.json._


  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("email_messages")

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[EmailMessage]] = {
    collection.find(query).one[EmailMessage]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[EmailMessage]] = {
    collection.find(query).cursor[EmailMessage].collect[List]()
  }

  override def insert(emailMessage: EmailMessage)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(emailMessage)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
