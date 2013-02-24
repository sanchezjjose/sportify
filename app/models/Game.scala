package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import java.util.Date

case class Game(startTime: String,
                address: String,
                gym: String,
                opponent: String,
                result: String)
                // players: List[User]) // is this covered by the user_games table?

object Game {

	/**
   * Insert a new game.
   *
   * @param game The game values
   */
  /*def insert(game: Game) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into games values (
            (select next value for game_seq),
            {start_time}, {address}, {gym}, {opponent}, {result}
          )
        """
      ).on(
        'start_time -> game.startTime,
        'address -> game.address,
        'gym -> game.gym,
        'opponent -> game.opponent,
        'result -> game.result
      ).executeUpdate()
    }
  }*/
}