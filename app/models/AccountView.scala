package models

case class AccountView (
  teamViewModel: TeamViewModel,
  playerId: Long,
  email: String,
  password: Option[String],
  firstName: String,
  lastName: String,
  number: Int,
  phoneNumber: Option[String],
  position: Option[String],
  isAdmin: Boolean
)
