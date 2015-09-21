package controllers

import models._
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import util.{Loggable, Helper, RequestHelper}


object Admin extends Controller
  with Helper
  with RequestHelper
  with Loggable
  with Secured {

  val gameForm: Form[GameForm] = Form(
    mapping(
      "number" -> optional(text),
      "start_time" -> text,
      "address" -> text,
      "gym" -> text,
      "location_details" -> optional(text),
      "opponent" -> text,
      "result" -> optional(text)
    ) { (number, startTime, address, gym, locationDetails, opponent, result) =>

      GameForm(
        number,
        startTime,
        address,
        gym,
        locationDetails,
        opponent,
        result
      )

    } { (game: GameForm) =>

      Some((
        game.number,
        game.startTime,
        game.address,
        game.gym,
        game.locationDetails,
        game.opponent,
        game.result))
    }
  )

  def save(teamId: Long, seasonId: Long, isPlayoffGame: String) = Action { /*implicit user =>*/ implicit request =>
    gameForm.bindFromRequest.fold(
      errors => {
        log.error("There was a problem adding a new game", errors)

        Redirect(routes.Schedule.schedule(teamId)).flashing(
          "failure" -> "There was a problem with adding a new game."
        )},

      gameForm => {

        try {
          // Ensure date format was correct
          DateTime.parse(gameForm.startTime, Game.gameDateFormat)

          Season.findById(seasonId).map { season =>

            val newGame = gameForm.toNewGame(seasonId, isPlayoffGame.toBoolean)
            Game.create(newGame)

            // Add game to season and update
            val updatedSeason = season.copy(game_ids = season.game_ids + newGame._id)

            Season.update(updatedSeason)

            // Also add the game to opponent's season if opponent has a team
            Team.findByName(gameForm.opponent).map { opponentTeam =>
              opponentTeam.season_ids.map { opponentSeasonId =>
                Season.findById(opponentSeasonId).find(season => season.is_current_season).map { opponentSeason =>
                  val tVm = buildTeamView(teamId)
                  val oppNewGame = gameForm.toNewGame(opponentSeasonId, isPlayoffGame.toBoolean).copy(opponent = tVm.current.name)
                  val updatedSeason = opponentSeason.copy(game_ids = opponentSeason.game_ids + oppNewGame._id)

                  Game.create(oppNewGame)
                  Season.update(updatedSeason)
                }
              }
            }
          }

          Redirect(routes.Schedule.schedule(teamId))


        } catch {

          case e: Exception => {
            log.error("There was a problem with adding a new game", e)

            Redirect(routes.Schedule.schedule(teamId)).flashing(
              "failure" -> "There was a problem with adding a new game. Make sure the date format is correct."
            )
          }
        }
      }
    )
  }

  def edit(teamId: Long, gameId: Long) = Action { /*implicit user =>*/ implicit request =>
    val game = Game.findById(gameId).get

    Ok(Json.toJson(
      Map(
        "team_id" -> Json.toJson(teamId),
        "game_id" -> Json.toJson(gameId),
        "number" -> Json.toJson(game.number),
        "start_time" -> Json.toJson(game.start_time),
        "address" -> Json.toJson(game.address),
        "gym" -> Json.toJson(game.gym),
        "location_details" -> Json.toJson(game.location_details),
        "opponent" -> Json.toJson(game.opponent),
        "result" -> Json.toJson(game.result)
      ))
    )
  }

  def update(teamId: Long, gameId: Long, isPlayoffGame: String) = Action { /*implicit user =>*/ implicit request =>
   gameForm.bindFromRequest.fold(
     errors => {
       log.error(errors.toString)

       Redirect(routes.Schedule.schedule(teamId)).flashing(
         "failure" -> "There was a problem with adding a new game."
       )},


     gameForm => {

       try {
         // Validate date format was correct
         DateTime.parse(gameForm.startTime, Game.gameDateFormat)
         val game = gameForm.toGame(gameId, isPlayoffGame.toBoolean)

         Game.update(game)

         Redirect(routes.Schedule.schedule(teamId))
       } catch {
         case e: Exception => {
           log.error("There was a problem with adding a new game", e)

           Redirect(routes.Schedule.schedule(teamId)).flashing(
             "failure" -> "There was a problem with adding a new game. Make sure the date format is correct."
           )
         }
       }
     }
   )
  }

  def delete(teamId: Long, seasonId: Long, gameId: Long) = Action { /*implicit user =>*/ implicit request =>
    val season = Season.findById(seasonId).get
    val updatedSeason = season.copy(game_ids = season.game_ids - gameId)

    Season.update(updatedSeason)
    Game.remove(gameId)

    Redirect(routes.Schedule.schedule(teamId))
  }
}

