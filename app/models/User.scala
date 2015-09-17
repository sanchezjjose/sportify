package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.sportify.db.MongoManagerFactory
import utils.CustomPlaySalatContext
import CustomPlaySalatContext._
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import utils.Helper


/**
 * A model representation of a user. The user could also optionally be a facebook user,
 * depending on the method of registration.
 */
case class User (_id: Long,
                 email: String,
                 password: Option[String],
                 first_name: String,
                 last_name: String,
                 players: Set[Player] = Set.empty[Player],
                 is_admin: Boolean = false,
                 phone_number: Option[String] = None) {

  def hashPassword: String = {
    User.hashPassword(password.get)
  }

  def fullName: String = {
    "%s %s".format(first_name, last_name)
  }
}

object User extends Helper {

  implicit val userWrites: Writes[User] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "email").write[String] and
    (JsPath \ "password").write[Option[String]] and
    (JsPath \ "first_name").write[String] and
    (JsPath \ "last_name").write[String] and
    (JsPath \ "player").write[Set[Player]] and
    (JsPath \ "is_admin").write[Boolean] and
    (JsPath \ "phone_number").write[Option[String]]
  )(unlift(User.unapply))

  def authenticate(email: String, password: String): Option[User] = {
    findByEmail(email).filter(user => BCrypt.checkpw(password, user.password.get))
  }

  def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt())
  }


  /*
   * MONGO API -- TODO: move to separate DB Trait
   */

  private val mongoManager = MongoManagerFactory.instance

  def findById(id: Long): Option[User] = {
    val dbObject = mongoManager.users.findOne( MongoDBObject("_id" -> id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByEmail(email: String): Option[User] = {
    val dbObject = mongoManager.users.findOne( MongoDBObject("email" -> email) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByEmailAndPassword(email: String, password: String): Option[User] = {
    val dbObject = mongoManager.users.findOne( MongoDBObject("email" -> email, "password" -> password) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByPlayerId(id: Long): Option[User] = {
    val dbObject = mongoManager.users.findOne( MongoDBObject("players.id" -> id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findAll: Iterator[User] = {
    val dbObjects = mongoManager.users.find()
    for (x <- dbObjects) yield grater[User].asObject(x)
  }

  def create(user: User) = {
    val dbo = grater[User].asDBObject(user)
    dbo.put("_id", generateRandomId())
    dbo.put("password", user.hashPassword)

    mongoManager.users += dbo
  }

  def create(email: Option[String], password: Option[String], firstName: String, lastName: Option[String], players: Set[Player]) = {
    val id = generateRandomId()
    val user = User(id, email.getOrElse(""), password, firstName, lastName.getOrElse(""), players)
    val dbo = grater[User].asDBObject(user)
    password.map(_ => dbo.put("password", user.hashPassword))

    mongoManager.users += dbo
  }

  def update(user: User, data: AccountView) = {
    mongoManager.users.update(MongoDBObject("_id" -> user._id),
      $set("email" -> data.email,
           "password" -> data.password.filter(_.trim != "").map(hashPassword).getOrElse(user.password),
           "first_name" -> data.firstName,
           "last_name" -> data.lastName,
           "phone_number" -> data.phoneNumber)
    )
  }

  def updatePlayer(user: User, data: AccountView) = {
    mongoManager.users.update(MongoDBObject("_id" -> user._id, "players.id" -> data.playerId),
      $set(
        "players.$.position" -> data.position,
        "players.$.number" -> data.number)
    )
  }

  def updateAccessToken(access_token: String, user_id: String) = {
    mongoManager.users.update(MongoDBObject("facebook_user.user_id" -> user_id),
      $set("facebook_user.access_token" -> access_token)
    )
  }

  def delete(user: User) = {
    val dbo = grater[User].asDBObject(user)
    mongoManager.users -= dbo
  }
}
