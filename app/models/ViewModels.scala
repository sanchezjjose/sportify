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


case class HomepageView (
  teamViewModel: TeamViewModel,
  nextGameOpt: Option[Game],
  playersIn: Set[User],
  playersOut: Set[User]
)


case class RosterView (
  teams: TeamViewModel,
  players: List[PlayerViewModel]
)


case class ScheduleView (
  teamViewModel: TeamViewModel,
  currentSeasonOpt: Option[Season],
  games: List[Game],
  nextGameOpt: Option[Game]
)


case class PlayerViewModel (
  id: Long,
  name: String,
  number: Int,
  phoneNumber: Option[String],
  position: Option[String]
)