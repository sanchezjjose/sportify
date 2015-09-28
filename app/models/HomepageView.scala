package models


case class HomepageView (
  teamViewModel: TeamViewModel,
  nextGameOpt: Option[Game],
  playersIn: Set[User],
  playersOut: Set[User]
)
