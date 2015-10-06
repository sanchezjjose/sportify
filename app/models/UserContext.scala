package models


case class UserContext (
  user: User,
  players: Set[Player],
  teams: Set[Team]

) {

  def getTeam(teamId: Long): Team = {
    teams.find(team => team._id == teamId).get
  }

  def getOtherTeams(teamId: Long): Set[Team] = {
    teams.filter(team => team._id != teamId)
  }

  def getPlayerOnTeam(teamId: Long): Player = {
    players.find( p =>
      teams.exists( t =>
        t.player_ids.contains(p._id)
      )
    ).get
  }
}
