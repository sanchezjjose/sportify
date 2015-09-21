package models

import org.joda.time.DateTime

case class EmailMessage(
  _id: Long,
  user_id: Long,
  game_id: Long,
  send_at: Long,
  sent_at: Option[Long],
  num_attempts: Int,
  recipient: String,
  status: String = "unsent"
)

object EmailMessage {

  private val mongoManager = MongoManagerFactory.instance

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
}
