package models


case class ScheduleView(
  teams: TeamViewModel,
  currentSeason: Option[Season],
  games: List[Game],
  nextGame: Option[Game]
)
