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

  def find(query: BSONDocument): Future[List[JsObject]]

  def save(document: BSONDocument): Future[WriteResult]

  def update(user: User, data: AccountView): Future[WriteResult]

  def updatePlayer(user: User, data: AccountView): Future[WriteResult]

  def updateAccessToken(access_token: String, user_id: String): Future[WriteResult]

  def delete(user: User): Future[WriteResult]
}

class UserMongoDb(reactiveMongoApi: ReactiveMongoApi) extends UserDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  // TODO: Make this method Reactive
  def authenticate(email: String, password: String): Option[User] = {

    Await
      .result(findOne(BSONDocument("email" -> email, "password" -> password)), Duration(10, TimeUnit.SECONDS))
      .filter(user => BCrypt.checkpw(password, user.password.get))
  }

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]] = {
    collection.find(query).one[User]
  }

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]] = {
    collection.find(query).cursor[User].collect[List]()
  }

  def save(document: BSONDocument) = collection.save(document)

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
