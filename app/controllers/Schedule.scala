package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.data.Forms.text
import org.joda.time.DateTime

object Schedule extends Controller with Loggable with Secured {

  def schedule = IsAuthenticated { user => implicit request =>
    val currentSeason = Season.findCurrentSeason().get
    val games = currentSeason.gameIds.flatMap(Game.findById).toList.sortBy(_.number)

    Ok(views.html.schedule(gameForm, currentSeason, Game.findNextGame, games)(user))
  }

	def submit = Action { implicit request =>
    val gameId = request.rawQueryString.split("=")(2).toInt
    val game : Option[Game] = Game.findById(gameId)
    val user = User.loggedInUser

    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {

      // Add user to game.
      game.get.players_in += user.player.get
      game.get.players_out -= user.player.get
      Game.update(game.get)

      Ok(Json.toJson(
        Map(
          "status" -> Json.toJson("in"),
          "msg" -> Json.toJson("You are playing. See you there!")
        )
      ))
    } else {

      // Remove user from game.
      game.get.players_in -= user.player.get
      game.get.players_out += user.player.get
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
      "start_time" -> text,
      "address" -> text,
      "gym" -> text,
      "location_details" -> optional(text),
      "opponent" -> text,
      "result" -> optional(text)
    ) { (startTime, address, gym, locationDetails, opponent, result) =>

      GameForm(startTime,
        address,
        gym,
        locationDetails,
        opponent,
        result)

    } { (game: GameForm) =>

      Some((game.startTime,
        game.address,
        game.gym,
        game.locationDetails,
        game.opponent,
        game.result))
    }
  )

  // TODO: move to Game controller
  def changeRsvpStatus(game_id: Long, user_id: String, status: String) = Action {
    val game = Game.findById(game_id).get
    val user = User.findById(user_id.toLong).get

    if (status == "in") {
      game.players_in += user.player.get
      game.players_out -= user.player.get
    } else if (status == "out") {
      game.players_in -= user.player.get
      game.players_out += user.player.get
    }

    Game.update(game)

    Redirect(routes.Application.home)
  }

  // TODO: move to Game controller
  def save(seasonId: Long, isPlayoffGame: String) = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      errors => {
        log.error(errors.toString)

        Redirect(routes.Schedule.schedule).flashing(
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
            season.gameIds += newGame._id
            Season.update(season)
          }

          Redirect(routes.Schedule.schedule)
        } catch {
          case e: Exception => {
            log.error("There was a problem with adding a new game", e)

            Redirect(routes.Schedule.schedule).flashing(
              "failure" -> "There was a problem with adding a new game. Make sure the date format is correct."
            )
          }
        }
      }
    )
  }

  // TODO: move to Game controller
  def edit(gameId: Long) = IsAuthenticated { user => implicit request =>
   val game = Game.findById(gameId).get

    Ok(Json.toJson(
      Map(
        "game_id" -> Json.toJson(game._id),
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
  def update(gameId: Long, gameNumber: Int, isPlayoffGame: String) = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      errors => {
        log.error(errors.toString)

        Redirect(routes.Schedule.schedule).flashing(
          "failure" -> "There was a problem with adding a new game."
        )},

      gameForm => {

        try {
          // Validate date format was correct
          DateTime.parse(gameForm.startTime, Game.gameDateFormat)
          val game = gameForm.toGame(gameId, gameNumber, isPlayoffGame.toBoolean)

          Game.update(game)

          Redirect(routes.Schedule.schedule)
        } catch {
          case e: Exception => {
            log.error("There was a problem with adding a new game", e)

            Redirect(routes.Schedule.schedule).flashing(
              "failure" -> "There was a problem with adding a new game. Make sure the date format is correct."
            )
          }
        }
      }
    )
  }

  // TODO: move to Game controller
  def delete(seasonId: Long, gameId: Long) = IsAuthenticated { user => implicit request =>
    val season = Season.findById(seasonId).get

    Game.remove(gameId)
    season.gameIds -= gameId
    Season.update(season)

    Redirect(routes.Schedule.schedule)
  }
}