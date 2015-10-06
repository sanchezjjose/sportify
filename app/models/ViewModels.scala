package models


case class TeamViewModel (
  selectedTeam: Team,
  otherTeams: Iterable[Team]
)


case class AccountViewModel (
  teamViewModel: TeamViewModel,
  userId: Long,
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


case class HomepageViewModel (
  teamViewModel: TeamViewModel,
  nextGameOpt: Option[Game]
)


case class RosterViewModel (
  teamViewModel: TeamViewModel,
  players: List[PlayerViewModel]
)


case class ScheduleViewModel (
  teamViewModel: TeamViewModel,
  currentSeasonOpt: Season,
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