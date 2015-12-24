package models


case class TeamViewModel (
  active_team: Team,
  other_teams: Set[Team]
)

case class AccountViewModel (
  active_team: Team,
  other_teams: Set[Team],
  user_id: Long,
  player_id: Long,
  email: String,
  password: Option[String],
  first_name: String,
  last_name: String,
  number: Int,
  phone_number: Option[String],
  position: Option[String],
  is_admin: Boolean
)

case class HomepageViewModel (
  active_team: Team,
  other_teams: Set[Team],
  next_game: Option[Game],
  player_id: Long,
  players_in: Set[PlayerViewModel],
  players_out: Set[PlayerViewModel]
)

case class RosterViewModel (
  active_team: Team,
  other_teams: Set[Team],
  players: List[PlayerViewModel]
)

case class ScheduleViewModel (
  active_team: Team,
  other_teams: Set[Team],
  active_season: Season,
  games: List[Game],
  next_game: Option[Game]
)

case class PlayerViewModel (
  player_id: Long,
  full_name: String,
  number: Int,
  phone_number: Option[String],
  position: Option[String]
)

case class RsvpViewModel (
  players_in: Set[PlayerViewModel],
  players_out: Set[PlayerViewModel]
)