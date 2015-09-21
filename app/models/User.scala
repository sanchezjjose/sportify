package models

import api.UserDb
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.functional.syntax._
import util.Helper


case class User (
  _id: Long, 
  email: String, 
  password: Option[String], 
  first_name: String, 
  last_name: String, 
  players: Set[Player] = Set.empty[Player], 
  is_admin: Boolean = false, 
  phone_number: Option[String] = None
) {

  def hashPassword: String = {
    BCrypt.hashpw(password.get, BCrypt.gensalt())
  }

  def fullName: String = {
    "%s %s".format(first_name, last_name)
  }
}

object JsonUserFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._
  import play.modules.reactivemongo.json.BSONFormats._

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val userFormat = Json.format[User]
}

object UserFields {
  val Id = "_id"
  val Email = "email"
  val Password = "password"
  val FirstName = "first_name"
  val LastName = "last_name"
  val Players = "players"
  val IsAdmin = "is_admin"
  val PhoneNumber = "phone_number"
}
