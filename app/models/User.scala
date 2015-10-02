package models

import org.mindrot.jbcrypt.BCrypt

case class User (
  _id: Long, 
  email: String, 
  password: Option[String], 
  first_name: String, 
  last_name: String, 
  team_ids: Set[Long] = Set.empty[Long],
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

object UserFields {
  val Id = "_id"
  val Email = "email"
  val Password = "password"
  val FirstName = "first_name"
  val LastName = "last_name"
  val TeamIds = "team_ids"
  val IsAdmin = "is_admin"
  val PhoneNumber = "phone_number"
}
