package models


case class ScheduleView (
  teamViewModel: TeamViewModel,
  currentSeasonOpt: Option[Season],
  games: List[Game],
  nextGameOpt: Option[Game]
)
