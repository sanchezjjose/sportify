package controllers

import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.data.Forms.text
import org.joda.time.DateTime

object Schedule extends Controller with Loggable with Secured {

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

  def schedule = IsAuthenticated { user => implicit request =>
    Ok(views.html.schedule(gameForm, Season.findCurrentSeason().get, Game.findNextGame, Game.findAllInCurrentSeason.toList)(user))
  }

  def rsvp(game_id: Long, user_id: String, status: String) = Action {
    val game = Game.findById(game_id)
    val user = User.findById(user_id.toLong).get

    if (status == "in") {
      game.get.players_in += user.player.get
      game.get.players_out -= user.player.get
      Game.update(game.get)
    }

    if (status == "out") {
      game.get.players_in -= user.player.get
      game.get.players_out += user.player.get
      Game.update(game.get)
    }

    Redirect(routes.Application.home)
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

  def save(isPlayoffGame: String, seasonId: Long) = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      errors => {
        log.error(errors.toString)

        Redirect(routes.Schedule.schedule).flashing(
        "failure" -> "There was a problem with adding a new game."
      )},

      gameForm => {

        try {
          // Ensure date format was correct
          DateTime.parse(gameForm.startTime, Game.format)

          Game.create(gameForm.toNewGame(isPlayoffGame.toBoolean))
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

  def edit(game_id: Long) = IsAuthenticated { user => implicit request =>
   val game = Game.findById(game_id).get

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

  def update(gameId: Long, gameNumber: Int, isPlayoffGame: String) = Action { implicit request =>
    gameForm.bindFromRequest.fold(
      errors => {
        log.error(errors.toString)

        Redirect(routes.Schedule.schedule).flashing(
          "failure" -> "There was a problem with adding a new game."
        )},

      gameForm => {

        try {
          // Ensure date format was correct
          DateTime.parse(gameForm.startTime, Game.format)
          Game.update(gameForm.toGame(gameId, gameNumber, isPlayoffGame.toBoolean))
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

  def delete(game_id: Long) = IsAuthenticated { user => implicit request =>
    Game.removeGame(game_id)
    Redirect(routes.Schedule.schedule)
  }
}