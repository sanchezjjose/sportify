package models


/**
 * Model of a team, which is made up of players and a specific sport.
 */
case class Team (id: Long,
                 name: String,
                 players: List[User],
                 sport: Sport)
