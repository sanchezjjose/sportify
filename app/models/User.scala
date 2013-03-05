package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import controllers.MongoManager

case class User(email: String,
                firstName: String,
                lastName: String,
                number: Int)

object User {

  var loggedInUser: User = _
  
  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    val dbObject = MongoManager.collection.findOne( MongoDBObject("email" -> email) )
    dbObject.map(o => grater[User].asObject(o))
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Iterator[User] = {
    val dbObjects = MongoManager.collection.find()
    for (x <- dbObjects) yield grater[User].asObject(x)
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    val dbObject = MongoManager.collection.findOne( MongoDBObject("email" -> email, "password" -> password) )
    dbObject.map(o => grater[User].asObject(o))
  }

  /**
   * Insert a new user.
   *
   * @param user The user values
   */
  def insert(user: User) = {
    val dbo = grater[User].asDBObject(user)
    dbo.put("password", "password")
    MongoManager.collection += dbo
  }
  
}
