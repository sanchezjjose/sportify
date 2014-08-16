package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.mongodb.casbah.Imports._
import controllers.MongoManager
import java.util.UUID
import CustomPlaySalatContext._
import play.api.libs.json._
import play.api.libs.functional.syntax._


/**
 * This should eventually be linked to a player, and season.
 * It will mean tracking historical data, per player and season.
 * It will also lend itself to finally be able to view data per game,
 * and eventually player profiles.
 */
case class PlayerStats(name: String,
                       gamesPlayed: String,
                       pointsPerGame: String,
                       assistsPerGame: String,
                       reboundsPerGame: String)

object PlayerHelper {

  def formatName(firstName: String, lastName: String): String = {
    "%s %s.".format(firstName, lastName.charAt(0))
  }
}

case class User(_id: String,
                email: String,
                firstName: String,
                lastName: String,
                number: Int,
                position: String,
                facebookUser: Option[FacebookUser] = None,
                isAdmin: Boolean = false)

object User {

  var loggedInUser: User = _

  implicit val userWrites: Writes[User] = (
    (JsPath \ "_id").write[String] and
    (JsPath \ "email").write[String] and
    (JsPath \ "firstName").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "number").write[Int] and
    (JsPath \ "position").write[String] and
    (JsPath \ "facebookUser").write[Option[FacebookUser]] and
    (JsPath \ "isAdmin").write[Boolean]
  )(unlift(User.unapply))

  /**
   * Retrieve a User from id.
   */
  def findByFacebookUserId(user_id: String): Option[User] = {
    val dbObject = MongoManager.usersColl.findOne( MongoDBObject("facebookUser.user_id" -> user_id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  /**
   * Retrieve a User from id.
   */
  def findById(_id: String): Option[User] = {
    val dbObject = MongoManager.usersColl.findOne( MongoDBObject("_id" -> _id) )
    dbObject.map(o => grater[User].asObject(o))
  }
  
  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    val dbObject = MongoManager.usersColl.findOne( MongoDBObject("email" -> email) )
    dbObject.map(o => grater[User].asObject(o))
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Iterator[User] = {
    val dbObjects = MongoManager.usersColl.find()
    for (x <- dbObjects) yield grater[User].asObject(x)
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    val dbObject = MongoManager.usersColl.findOne( MongoDBObject("email" -> email, "password" -> password) )
    dbObject.map(o => grater[User].asObject(o))
  }

  /**
   * Insert a new user.
   *
   * @param user The user values
   */
  def insert(user: User) = {
    val dbo = grater[User].asDBObject(user)
    dbo.put("_id", UUID.randomUUID().toString)
    dbo.put("password", "giltunit")

    MongoManager.usersColl += dbo
  }

  /**
   * Insert a new user with an option of a facebook user.
   */
  def insert(email: Option[String], firstName: String, lastName: Option[String], facebookUser: Option[FacebookUser]) = {
    val _id = if (facebookUser.isDefined) {
      facebookUser.get.user_id
    } else {
      UUID.randomUUID().toString
    }
    val user = User(_id, email.getOrElse(""), firstName, lastName.getOrElse(""), 0, "SF", facebookUser)
    val dbo = grater[User].asDBObject(user)
    dbo.put("password", "giltunit")

    MongoManager.usersColl += dbo
  }

  def updateAccountInformation(updatedUser: User) = {
    MongoManager.usersColl.update(MongoDBObject("_id" -> User.loggedInUser._id),
      $set("email" -> updatedUser.email,
        "firstName" -> updatedUser.firstName,
        "lastName" -> updatedUser.lastName,
        "number" -> updatedUser.number,
        "position" -> updatedUser.position)
    )
  }

  /**
   * Update access token.
   */
  def updateAccessToken(access_token: String, user_id: String) = {
    MongoManager.usersColl.update(MongoDBObject("facebookUser.user_id" -> user_id), $set("facebookUser.access_token" -> access_token))
  }

  def delete(user: User) = {
    val dbo = grater[User].asDBObject(user)
    MongoManager.usersColl -= dbo
  }
}
