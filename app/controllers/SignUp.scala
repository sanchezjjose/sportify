package controllers

import javax.inject.Inject

import api.UserMongoDb
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.bson.BSONDocument
import util.{Helper, RequestHelper}

case class PlayerData(
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  jerseyNumber: Int,
  position: Option[String],
  phoneNumber: Option[String],
  teamId: Long
)

class SignUp @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val userDb = new UserMongoDb(reactiveMongoApi)

  val signupForm: Form[PlayerData] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "first_name" -> nonEmptyText,
      "last_name" -> nonEmptyText,
      "number" -> number,
      "position" -> optional(text),
      "phone_number" -> optional(text),
      "team_id" -> longNumber
    )(PlayerData.apply)(PlayerData.unapply)
  )

  def submit = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => {
        BadRequest
      },
      data => {
        import UserFields._

        val teamId = data.teamId

        Team.findById(teamId).map { team =>

          val player = Player(
            id = Helper.generateRandomId(),
            number = data.jerseyNumber,
            position = data.position
          )

          val user = User(
            _id = Helper.generateRandomId(),
            email = data.email,
            password = Some(data.password),
            first_name = data.firstName,
            last_name = data.lastName,
            players = Set(player),
            phone_number = data.phoneNumber
          )

          val updatedTeam = team.copy(player_ids = team.player_ids + player.id)

          userDb.save(BSONDocument(
            Id -> user._id,
            Email -> user.email,
            Password -> user.password,
            FirstName -> user.first_name,
            LastName -> user.last_name,
            Players -> user.players,
            PhoneNumber -> user.phone_number
          ))

          Team.update(updatedTeam)

          Redirect(routes.Homepage.home(teamId))
            .withSession("user_info" -> user.email)
            .flashing("team_id" -> s"$teamId")

        }.getOrElse(BadRequest)
      })
  }
}