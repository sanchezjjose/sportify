package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
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
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("users.email") ~
    get[String]("users.first_name") ~
    get[String]("users.last_name") ~
    get[Int]("users.jersey_number") map {
      case email~firstName~lastName~number => User(email, firstName, lastName, number)
    }
  }
  
  // -- Queries
  
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
  def findAll: Seq[User] = {
//    DB.withConnection { implicit connection =>
//      SQL("select * from users").as(User.simple *)
//    }

    val dbObjects = MongoManager.collection.find()
    dbObjects.map(o => grater[User].asDBObject(o))
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
