package models


case class UserContext (
  selectedTeam: Team,
  otherTeams: List[Team],
  currentSeason: Season,
  nextGame: Game,
  user: User,
  player: Player,
  sport: Sport
)
