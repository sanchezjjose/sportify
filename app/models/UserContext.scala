package models


case class UserContext (
  user: User,
  players: Set[Player],
  teams: Set[Team]

//  playerOpt: Option[Player],
//  currentSeasonOpt: Option[Season],
//  teams: List[Team],
//  nextGame: Option[Game]

) {

  def getTeam(teamId: Long): Team = {
    teams.find(team => team._id == teamId).get
  }

  def getOtherTeams(teamId: Long): List[Team] = {
    teams.filter(team => team._id != teamId)
  }
}
