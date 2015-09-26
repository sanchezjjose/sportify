package models

case class EmailMessage (
  _id: Long,
  user_id: Long,
  game_id: Long,
  send_at: Long,
  sent_at: Option[Long],
  num_attempts: Int,
  recipient: String,
  status: String = "unsent"
)

object EmailMessageFields {
  val Id = "_id"
  val UserId = "user_id"
  val GameId = "game_id"
  val SendAt = "send_at"
  val SentAt = "sent_at"
  val NumAttempts = "num_attempts"
  val Recipient = "recipient"
  val Status = "status"
}
