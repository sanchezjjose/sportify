package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

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
    DB.withConnection { implicit connection =>
      SQL("select * from users where email = {email}").on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users").as(User.simple *)
    }
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from users where 
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }

  /**
   * Insert a new user.
   *
   * @param user The user values
   */
  def insert(user: User) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into users values (
            (select next value for user_seq),
            {email}, {password}, {first_name}, {last_name}, {jersey_number}
          )
        """
      ).on(
        'email -> user.email,
        'password -> "password",
        'first_name -> user.firstName,
        'last_name -> user.lastName,
        'jersey_number -> user.number
      ).executeUpdate()
    }
  }
  
}
