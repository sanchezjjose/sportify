package controllers

import javax.inject.Inject

import api.UserMongoDb
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONDocument
import util.RequestHelper

import scala.concurrent.Future

class Login @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents with RequestHelper {

  override val db = new UserMongoDb(reactiveMongoApi)

  val loginForm: Form[(String, String)] = {
    Form {
      tuple(
        "email" -> text,
        "password" -> text
      ) verifying("Invalid email or password.", result => result match {
        case (email: String, password: String) => db.authenticate(email, password).isDefined
      })
    }
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(

      formWithErrors => {
        Future successful Unauthorized("Invalid credentials")
      },

      credentials => {

        for {
          userOpt <- db.findOne(BSONDocument("email" -> credentials._1))
          currentTeam <- buildTeamView()(userOpt.get, request)

        } yield {
          val user = userOpt.get // TODO: handle Future[Option] the proper way
          val defaultTeamId = currentTeam.current._id

          Redirect(routes.Homepage.home(defaultTeamId))
            .withSession("user_info" -> user.email)
            .flashing("team_id" -> s"$defaultTeamId")
        }
      }
    )
  }
}
