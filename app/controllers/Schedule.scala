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
      "location_details" -> text,
      "opponent" -> text,
      "result" -> optional(text)
    ) { (startTime, address, gym, locationDetails, opponent, result) =>
      GameForm(startTime, address, gym, locationDetails, opponent, result)
    } { (game: GameForm) =>
      Some((game.startTime, game.address, game.gym, game.locationDetails, game.opponent, game.result))
    }
  )

  def schedule = IsAuthenticated { user => implicit request =>
    Ok(views.html.schedule(gameForm, Config.season, Game.findNextGame, Game.findAllInCurrentSeason.toList)(user))
  }

  def rsvp(game_id: Int, user_id: String, status: String) = Action {
    val game = Game.findByGameId(game_id)

    if (status == "in") {
      game.get.playersIn += user_id
      game.get.playersOut -= user_id
      Game.update(game.get)
    }

    if (status == "out") {
      game.get.playersIn -= user_id
      game.get.playersOut += user_id
      Game.update(game.get)
    }

    Redirect(routes.Application.home)
  }

	def submit = Action { implicit request =>

    val gameId = request.rawQueryString.split("=")(2).toInt
    val game : Option[Game] = Game.findByGameId(gameId)
    val userId = User.loggedInUser._id

    if(request.queryString.get("status").flatMap(_.headOption).get.contains("in")) {

      // Add user to game.
      game.get.playersIn += userId
      game.get.playersOut -= userId
      Game.update(game.get)

      Ok(Json.toJson(
        Map(
          "status" -> Json.toJson("in"),
          "msg" -> Json.toJson("You are playing. See you there!")
        )
      ))
    } else {

      // Remove user from game.
      game.get.playersIn -= userId
      game.get.playersOut += userId
      Game.update(game.get)

      Ok(Json.toJson(
        Map(
          "status" -> Json.toJson("out"),
          "msg" -> Json.toJson("Ok, you are not playing in this game. Maybe next time!")
        )
      ))
    }
	}

  def save(isPlayoffGame: String) = Action { implicit request =>
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

          Game.insert(gameForm.toNewGame(isPlayoffGame.toBoolean))
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

  def edit(game_id: Int) = IsAuthenticated { user => implicit request =>
   val game = Game.findByGameId(game_id).get

    Ok(Json.toJson(
      Map(
        "game_id" -> Json.toJson(game.game_id),
        "game_seq" -> Json.toJson(game.game_seq),
        "start_time" -> Json.toJson(game.startTime),
        "address" -> Json.toJson(game.address),
        "gym" -> Json.toJson(game.gym),
        "location_details" -> Json.toJson(game.locationDetails),
        "opponent" -> Json.toJson(game.opponent),
        "result" -> Json.toJson(game.result)
      )
    ))
  }

  def update(gameId: Int, gameSeq: Int, isPlayoffGame: String) = Action { implicit request =>
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
          Game.update(gameForm.toGame(gameId, gameSeq, isPlayoffGame.toBoolean))
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

  def delete(game_id: Int) = IsAuthenticated { user => implicit request =>
    Game.removeGame(game_id)
    Redirect(routes.Schedule.schedule)
  }
}