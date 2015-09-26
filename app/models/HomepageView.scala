package models


case class HomepageView (
  teams: TeamViewModel,
  nextGameInSeason: Option[Game],
  playersIn: Set[User],
  playersOut: Set[User]
)
