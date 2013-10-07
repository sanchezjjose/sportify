package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import controllers.MongoManager
import java.util.UUID

case class User(_id: String,
                email: String,
                firstName: String,
                lastName: String,
                number: Int,
                position: String,
                facebookUser: Option[FacebookUser] = None)

object User {

  var loggedInUser: User = _

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

  /**
   * Update access token.
   */
  def updateAccessToken(access_token: String, user_id: String) = {
    MongoManager.usersColl.update(MongoDBObject("facebookUser.user_id" -> user_id), $set("facebookUser.access_token" -> access_token))
  }
}
