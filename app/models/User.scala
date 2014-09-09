package models

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import controllers.{UserForm, MongoManager}
import models.CustomPlaySalatContext._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


/**
 * A model representation of a user. The user could also optionally be a facebook user,
 * depending on the method of registration.
 */
case class User (_id: Long,
                 email: String,
                 first_name: String,
                 last_name: String,
                 player: Option[Player] = None,
                 facebook_user: Option[FacebookUser] = None,
                 is_admin: Boolean = false)

object User extends Helper {

  var loggedInUser: User = _

  implicit val userWrites: Writes[User] = (
    (JsPath \ "_id").write[Long] and
    (JsPath \ "email").write[String] and
    (JsPath \ "first_name").write[String] and
    (JsPath \ "last_name").write[String] and
    (JsPath \ "player").write[Option[Player]] and
    (JsPath \ "facebook_user").write[Option[FacebookUser]] and
    (JsPath \ "is_admin").write[Boolean]
  )(unlift(User.unapply))
 

  def findById(id: Long): Option[User] = {
    val dbObject = MongoManager.users.findOne( MongoDBObject("_id" -> id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByFacebookUserId(user_id: Long): Option[User] = {
    val dbObject = MongoManager.users.findOne( MongoDBObject("facebook_user.user_id" -> user_id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByEmail(email: String): Option[User] = {
    val dbObject = MongoManager.users.findOne( MongoDBObject("email" -> email) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findByPlayerId(id: Long): Option[User] = {
    val dbObject = MongoManager.users.findOne( MongoDBObject("player.id" -> id) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def findAll: Iterator[User] = {
    val dbObjects = MongoManager.users.find()
    for (x <- dbObjects) yield grater[User].asObject(x)
  }

  def authenticate(email: String, password: String): Option[User] = {
    val dbObject = MongoManager.users.findOne( MongoDBObject("email" -> email, "password" -> password) )
    dbObject.map(o => grater[User].asObject(o))
  }

  def create(user: User) = {
    val dbo = grater[User].asDBObject(user)
    dbo.put("_id", generateRandomId())
    dbo.put("password", "giltunit") // TODO: move to Environment variable or hash in the DB

    MongoManager.users += dbo
  }

  def create(email: Option[String], firstName: String, lastName: Option[String], player: Option[Player], facebookUser: Option[FacebookUser]) = {
    val id = if (facebookUser.isDefined) {
      facebookUser.get.user_id.toLong
    } else {
      generateRandomId()
    }

    val user = User(id, email.getOrElse(""), firstName, lastName.getOrElse(""), player, facebookUser)
    val dbo = grater[User].asDBObject(user)
    dbo.put("password", "giltunit") // TODO: move to Environment variable or hash in the DB

    MongoManager.users += dbo
  }

  def update(data: UserForm) = {
    MongoManager.users.update(MongoDBObject("_id" -> User.loggedInUser._id),
      $set("email" -> data.email,
           "first_name" -> data.firstName,
           "last_name" -> data.lastName,
           "player.position" -> data.position,
           "player.number" -> data.number)
    )
  }

  def updateAccessToken(access_token: String, user_id: String) = {
    MongoManager.users.update(MongoDBObject("facebook_user.user_id" -> user_id), $set("facebook_user.access_token" -> access_token))
  }

  def delete(user: User) = {
    val dbo = grater[User].asDBObject(user)
    MongoManager.users -= dbo
  }
}

object UserHelper {

  def formatName(firstName: String, lastName: String): String = {
    "%s %s.".format(firstName, lastName.charAt(0))
  }
}


/**
 * A user who signed up with Facebook login.
 */
case class FacebookUser (access_token: String,
	                       user_id: String)

object FacebookUser {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val facebookUserWrites: Writes[FacebookUser] = (
    (JsPath \ "access_token").write[String] and
    (JsPath \ "user_id").write[String]
  )(unlift(FacebookUser.unapply))


  def findByAccessToken(access_token: String): Option[FacebookUser] = {
  	val dbObject = MongoManager.facebookAuths.findOne( MongoDBObject("access_token" -> access_token) )
  	dbObject.map( o => grater[FacebookUser].asObject(o) )
  }

  def findByUserId(user_id: String): Option[FacebookUser] = {
    val dbObject = MongoManager.facebookAuths.findOne( MongoDBObject("user_id" -> user_id) )
    dbObject.map( o => grater[FacebookUser].asObject(o) )
  }

  def create(access_token: String, user_id: String, email: Option[String], firstName: String, lastName: Option[String], player: Option[Player]) = {
  	val facebookUser = FacebookUser(access_token, user_id)
    val dbo = grater[FacebookUser].asDBObject(facebookUser)
    MongoManager.facebookAuths += dbo

    User.create(email, firstName, lastName, player, Option(facebookUser))
  }

  def updateAccessToken(access_token: String, user_id: String) = {
    MongoManager.facebookAuths.update(MongoDBObject("user_id" -> user_id), $set("access_token" -> access_token))
    MongoManager.users.update(MongoDBObject("facebook_user.user_id" -> user_id), $set("facebook_user.access_token" -> access_token))
  }
}