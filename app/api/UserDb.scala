package api

import java.util.concurrent.TimeUnit

import org.mindrot.jbcrypt.BCrypt
import models.{AccountView, Player}
import models.{User, JsonUserFormats}, JsonUserFormats._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.commands.WriteResult
import util.Helper

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


trait UserDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  // DEPRECATE

  def update(user: User, data: AccountView)(implicit ec: ExecutionContext): Future[WriteResult]

  def updatePlayer(user: User, data: AccountView)(implicit ec: ExecutionContext): Future[WriteResult]

  def updateAccessToken(access_token: String, user_id: String)(implicit ec: ExecutionContext): Future[WriteResult]

  def delete(user: User)(implicit ec: ExecutionContext): Future[WriteResult]
}

class UserMongoDb(reactiveMongoApi: ReactiveMongoApi) extends UserDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  def authenticate(email: String, password: String): Option[User] = {
    Await.result(findOne(BSONDocument("email" -> email, "password" -> password)), Duration(10, TimeUnit.SECONDS))
      .filter(user => BCrypt.checkpw(password, user.password.get))
  }

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]] = {
    collection.find(query).one[User]
  }

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]] = {
    collection.find(query).cursor[User].collect[List]()
  }

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.save(document)
  }

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }



  // DEPRECATE

  def update(user: User, data: AccountView) = {
    collection.update(MongoDBObject("_id" -> user._id),
      $set("email" -> data.email,
           "password" -> data.password.filter(_.trim != "").map(hashPassword).getOrElse(user.password),
           "first_name" -> data.firstName,
           "last_name" -> data.lastName,
           "phone_number" -> data.phoneNumber)
    )
  }

  def updatePlayer(user: User, data: AccountView) = {
    collection.update(MongoDBObject("_id" -> user._id, "players.id" -> data.playerId),
      $set(
        "players.$.position" -> data.position,
        "players.$.number" -> data.number)
    )
  }

  def delete(user: User) = {
    val dbo = grater[User].asDBObject(user)
    collection -= dbo
  }
}
