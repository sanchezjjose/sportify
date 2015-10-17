package controllers

import javax.inject.Inject
import api.MongoManager
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.{Helper, RequestHelper}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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

  override val mongoDb = new MongoManager(reactiveMongoApi)

  private val form: Form[PlayerData] = Form(
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

  def submit = Action.async { implicit request =>

    form.bindFromRequest.fold(

      errors => {
        Future successful BadRequest
      },

      data => {

        val teamId = data.teamId

        mongoDb.teams.findOne(Json.obj(TeamFields.Id -> teamId)).map { teamOpt =>

          teamOpt.map { team =>

            val playerId = Helper.generateRandomId()
            val userId = Helper.generateRandomId()

            val player = Player(
              _id = playerId,
              number = data.jerseyNumber,
              position = data.position
            )

            val user = User(
              _id = userId,
              email = data.email,
              password = Some(data.password),
              first_name = data.firstName,
              last_name = data.lastName,
              player_ids = Set(player._id),
              team_ids = Set(team._id),
              is_admin = false,
              phone_number = data.phoneNumber
            )

            // TODO: both save commands should happen as a transaction
            mongoDb.players.insert(player)
            mongoDb.users.insert(user)

            // Add player to the team
            val updatedTeam = team.copy(player_ids = team.player_ids + player._id)
            mongoDb.teams.update(
              Json.obj(TeamFields.Id -> team._id),
              Json.obj("$set" -> Json.obj(
                TeamFields.PlayerIds -> updatedTeam.player_ids
              ))
            )

            Redirect(routes.Homepage.home(teamId))
              .withSession("user_info" -> user.email)
              .flashing("team_id" -> s"$teamId")

          }.getOrElse {
            Results.NotFound
          }
        }
      }
    )
  }
}
