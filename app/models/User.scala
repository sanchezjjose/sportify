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
                position: String)

object User {

  var loggedInUser: User = _

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
  
}
