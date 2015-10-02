package api

import java.util.concurrent.TimeUnit

import models.User
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


trait UserDao {

  // TODO: move this to a UserClient API
  def authenticate(email: String, password: String)(implicit ec: ExecutionContext): Option[User]

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[User]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[User]]

  def insert(user: User)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class UserMongoDao(reactiveMongoApi: ReactiveMongoApi) extends UserDao {

  // BSON-JSON conversions
  import models.JsonFormats._
  import play.modules.reactivemongo.json._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  override def authenticate(email: String, password: String)(implicit ec: ExecutionContext): Option[User] = {
    Await.result(findOne(Json.obj("email" -> email, "password" -> password)), Duration(10, TimeUnit.SECONDS))
      .filter(user => BCrypt.checkpw(password, user.password.get))
  }

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[User]] = {
    collection.find(query).one[User]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[User]] = {
    collection.find(query).cursor[User].collect[List]()
  }

  override def insert(user: User)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(user)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
