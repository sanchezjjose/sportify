package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.data.Forms.text
import org.joda.time.DateTime
import utils.{Loggable, Helper}


object Schedule extends Controller with Helper with Loggable with Secured {

  def schedule(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    val tVm = buildTeamView(teamId)
    val currentSeason = tVm.current.season_ids.flatMap(Season.findById).find(_.is_current_season)
    val games = currentSeason.map(_.game_ids.flatMap(Game.findById).toList.sortBy(_.number)).getOrElse(List.empty[Game])
    val nextGameInSeason = currentSeason.flatMap(s => Game.getNextGame(s.game_ids))

    render {
      case Accepts.Html() => Ok(views.html.schedule(gameForm, currentSeason, nextGameInSeason, games, tVm))
      case Accepts.Json() => Ok(Json.toJson("{\"game_id\":1}"))
    }
  }

	def submit(teamId: Long) = IsAuthenticated { implicit user => implicit request =>
    val gameId = request.rawQueryString.split("=")(2).toInt
    val game: Option[Game] = Game.findById(gameId)
    val playerId = buildPlayerView(teamId).id

    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {

      // Add user to game.
      game.get.players_in += playerId
      game.get.players_out -= playerId
      Game.update(game.get)

      Ok(Json.toJson(
        Map(
          "status" -> Json.toJson("in"),
          "msg" -> Json.toJson("You are playing. See you there!")
        )
      ))
    } else {

      // Remove user from game.
      game.get.players_in -= playerId
      game.get.players_out += playerId
      Game.update(game.get)

      Ok(Json.toJson(
        Map(
          "status" -> Json.toJson("out"),
          "msg" -> Json.toJson("Ok, you are not playing in this game. Maybe next time!")
        )
      ))
    }
	}

  // TODO: move to Game controller
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

      GameForm(number,
        startTime,
        address,
        gym,
        locationDetails,
        opponent,
        result)

    } { (game: GameForm) =>

      Some((game.number,
        game.startTime,
        game.address,
        game.gym,
        game.locationDetails,
        game.opponent,
        game.result))
    }
  )

  // TODO: move to Game controller
  def changeRsvpStatus(teamId: Long, game_id: Long, status: String) = IsAuthenticated { implicit user => implicit request =>
    val game = Game.findById(game_id).get
    val playerId = buildPlayerView(teamId).id

    if (status == "in") {
      game.players_in += playerId
      game.players_out -= playerId
    } else if (status == "out") {
      game.players_in -= playerId
      game.players_out += playerId
    }

    Game.update(game)

    Redirect(routes.Homepage.home(buildTeamView(teamId).current._id))
  }

  // TODO: move to Game controller
  def save(teamId: Long, seasonId: Long, isPlayoffGame: String) = IsAuthenticated { implicit user => implicit request =>
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
            season.game_ids += newGame._id
            Season.update(season)

            // Also add the game to opponent's season if opponent has a team
            Team.findByName(gameForm.opponent).map { opponentTeam =>
              opponentTeam.season_ids.map { opponentSeasonId =>
                Season.findById(opponentSeasonId).find(season => season.is_current_season).map { opponentSeason =>
                  val tVm = buildTeamView(teamId)
                  val oppNewGame = gameForm.toNewGame(opponentSeasonId, isPlayoffGame.toBoolean).copy(opponent = tVm.current.name)
                  Game.create(oppNewGame)

                  opponentSeason.game_ids += oppNewGame._id
                  Season.update(opponentSeason)
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

  // TODO: move to Game controller
  def edit(teamId: Long, gameId: Long) = IsAuthenticated { implicit user => implicit request =>
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
      )
    ))
  }

  // TODO: move to Game controller
  def update(teamId: Long, gameId: Long, isPlayoffGame: String) = IsAuthenticated { implicit user => implicit request =>
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

  // TODO: move to Game controller
  def delete(teamId: Long, seasonId: Long, gameId: Long) = IsAuthenticated { user => implicit request =>
    val season = Season.findById(seasonId).get

    // remove from season first
    season.game_ids -= gameId
    Season.update(season)

    // remove game
    Game.remove(gameId)

    Redirect(routes.Schedule.schedule(teamId))
  }
}