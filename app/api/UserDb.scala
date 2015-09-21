package api

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

import scala.concurrent.{ExecutionContext, Future}


trait UserDb {

  def find(query: BSONDocument): Future[List[JsObject]]

  def create(user: User): Unit

  def create(email: Option[String], password: Option[String], firstName: String, lastName: Option[String], players: Set[Player]): Unit

  def update(user: User, data: AccountView): Unit

  def updatePlayer(user: User, data: AccountView): Unit

  def updateAccessToken(access_token: String, user_id: String): Unit

  def delete(user: User): Unit
}

class UserMongoDb(reactiveMongoApi: ReactiveMongoApi) extends UserDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  def authenticate(email: String, password: String): Option[User] = {
    val query = BSONDocument("email" -> email, "password" -> password)
//    userOpt.filter(user => BCrypt.checkpw(password, user.password.get))
    val x = for {
      userOpt: Future[Option[User]] <- findOne(query)
      user: User <- userOpt.get
      pw <- BCrypt.checkpw(password, user.password.get)
    } yield {
      user
    }

    x
  }

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]] = {
    collection.find(query).one[User]
  }

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]] = {
    collection.find(query).cursor[User].collect[List]()
  }

//  def findByEmail(email: String): Option[User] = {
//    val dbObject = collection.findOne( MongoDBObject("email" -> email) )
//    dbObject.map(o => grater[User].asObject(o))
//  }
//
//  def findByEmailAndPassword(email: String, password: String): Option[User] = {
//    val dbObject = collection.findOne( MongoDBObject("email" -> email, "password" -> password) )
//    dbObject.map(o => grater[User].asObject(o))
//  }
//
//  def findByPlayerId(id: Long): Option[User] = {
//    val dbObject = collection.findOne( MongoDBObject("players.id" -> id) )
//    dbObject.map(o => grater[User].asObject(o))
//  }
//
//  def findAll: Future[List[JsObject]] =
//    collection.find(Json.obj()).cursor[JsObject].collect[List]()

  def create(user: User) = {
    val dbo = grater[User].asDBObject(user)
    dbo.put("_id", generateRandomId())
    dbo.put("password", user.hashPassword)

    collection += dbo
  }

  def create(email: Option[String], password: Option[String], firstName: String, lastName: Option[String], players: Set[Player]) = {
    val id = generateRandomId()
    val user = User(id, email.getOrElse(""), password, firstName, lastName.getOrElse(""), players)
    val dbo = grater[User].asDBObject(user)
    password.map(_ => dbo.put("password", user.hashPassword))

    collection += dbo
  }

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
