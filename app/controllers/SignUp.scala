package controllers

import javax.inject.Inject
import api.MongoManager
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONDocument
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

  override val db = new MongoManager(reactiveMongoApi)

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

        db.teamDb.findOne(BSONDocument(TeamFields.Id -> teamId)).map { teamOpt =>
          val team = teamOpt.get // TODO: handle Options the proper way

          val playerId = Helper.generateRandomId()
          val userId = Helper.generateRandomId()

          val user = User(
            _id = userId,
            email = data.email,
            password = Some(data.password),
            first_name = data.firstName,
            last_name = data.lastName,
            player_ids = Set(playerId),
            phone_number = data.phoneNumber
          )

          val player = Player(
            id = playerId,
            user_id =userId,
            number = data.jerseyNumber,
            position = data.position
          )

          // TODO: both save commands should happen as a transaction

          db.playerDb.save(BSONDocument(
            PlayerFields.Id -> player.id,
            PlayerFields.Number -> player.number,
            PlayerFields.Position -> player.position
          ))

          db.userDb.save(BSONDocument(
            UserFields.Id -> user._id,
            UserFields.Email -> user.email,
            UserFields.Password -> user.password,
            UserFields.FirstName -> user.first_name,
            UserFields.LastName -> user.last_name,
            UserFields.PlayerIds -> user.player_ids,
            UserFields.PhoneNumber -> user.phone_number
          ))

          // Add player to the team
          val updatedTeam = team.copy(player_ids = team.player_ids + player.id)

          db.teamDb.update(
            BSONDocument(TeamFields.Id -> team._id),
            BSONDocument("$set" ->
              BSONDocument(TeamFields.PlayerIds -> updatedTeam.player_ids)
            )
          )

          Redirect(routes.Homepage.home(teamId))
            .withSession("user_info" -> user.email)
            .flashing("team_id" -> s"$teamId")
        }
      })
  }
}