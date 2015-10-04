package controllers

import javax.inject.Inject
import api.MongoManager
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import util.RequestHelper
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class Login @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new MongoManager(reactiveMongoApi)

  val loginForm: Form[(String, String)] = {
    Form {
      tuple(
        "email" -> text,
        "password" -> text
      ) verifying("Invalid email or password.", result => result match {
        case (email: String, password: String) => db.users.authenticate(email, password).isDefined
      })
    }
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(

      formWithErrors => {
        Future successful Unauthorized("Invalid credentials")
      },

      credentials => {
        val (username, _) = credentials

        for {
          userOpt <- db.users.findOne(Json.obj("email" -> username))
          userContext <- buildUserContext(userOpt.get)

        } yield {
          val user = userOpt.get
          val defaultTeamId = userContext.teams.head._id

          Redirect(routes.Homepage.home(defaultTeamId))
            .withSession (
              "user_info" -> user.email,
              "team_id" -> s"$defaultTeamId"
            )
        }
      }
    )
  }
}
