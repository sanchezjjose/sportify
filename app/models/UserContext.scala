package models


case class UserContext (
  user: User,
  player: Player,
  currentSeason: Season,
  teams: List[Team],
  nextGame: Option[Game],
  sport: Sport

) {

  def getTeam(teamId: Long): Team = teams.find(team => team._id == teamId).get

  def getOtherTeams(teamId: Long): List[Team] = teams.filter(team => team._id != teamId)
}
