package api

import models.{EmailMessage, Team}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{Future, ExecutionContext}

trait EmailMessageDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[EmailMessage]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[EmailMessage]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class EmailMessageMongoDb(reactiveMongoApi: ReactiveMongoApi) extends EmailMessageDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("email_messages")

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[EmailMessage]] = ???

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[EmailMessage]] = ???

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???
}

/*

def insert(message: EmailMessage) = {
  val dbo = grater[EmailMessage].asDBObject(message)
  mongoManager.emailMessages += dbo
}

def findByGameId(game_id: Int): Option[EmailMessage] = {
  val dbObject = mongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id))
  dbObject.map(o => grater[EmailMessage].asObject(o))
}

def findByGameIdAndRecipient(game_id: Long, recipient: String): Option[EmailMessage] = {
  val dbObject = mongoManager.emailMessages.findOne(MongoDBObject("game_id" -> game_id, "recipient" -> recipient))
  dbObject.map(o => grater[EmailMessage].asObject(o))
}

def findUnsent(game_id: Long): Iterator[EmailMessage] = {
  val dbObjects = mongoManager.emailMessages.find(
    MongoDBObject("game_id" -> game_id,
      "status" -> "unsent",
      "send_at" -> MongoDBObject("$lt" -> DateTime.now().getMillis),
      "num_attempts" -> MongoDBObject("$lt" -> 5))
  )
  for (x <- dbObjects) yield grater[EmailMessage].asObject(x)
}

def markAsSent(message: EmailMessage) = {
  mongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
    $set("sent_at" -> Some(DateTime.now().getMillis),
      "status" -> "sent"
    )
  )
}

def updateNumAttempts(message: EmailMessage) = {
  mongoManager.emailMessages.update(MongoDBObject("_id" -> message._id),
    $set("num_attempts" -> (message.num_attempts + 1))
  )
}


 */