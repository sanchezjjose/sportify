package models

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import controllers.MongoManager

case class FacebookUser (
	access_token: String,
	user_id: String
)

object FacebookUser {

  def findByAccessToken(access_token: String): Option[FacebookUser] = {
  	val dbObject = MongoManager.facebookAuthColl.findOne( MongoDBObject("access_token" -> access_token) )
  	dbObject.map( o => grater[FacebookUser].asObject(o) )
  }

  def findByUserId(user_id: String): Option[FacebookUser] = {
    val dbObject = MongoManager.facebookAuthColl.findOne( MongoDBObject("user_id" -> user_id) )
    dbObject.map( o => grater[FacebookUser].asObject(o) )
  }

  /**
   * Add new users FB information.
   */
  def insert(access_token: String, user_id: String, email: Option[String], firstName: String, lastName: Option[String]) = {
  	val facebookUser = FacebookUser(access_token, user_id)
    val dbo = grater[FacebookUser].asDBObject(facebookUser)
    MongoManager.facebookAuthColl += dbo

    // Create a user with an option of a facebookUser
    User.insert(email, firstName, lastName, Option(facebookUser))
  }

  /**
   * Update access token.
   */
  def updateAccessToken(access_token: String, user_id: String) = {
    MongoManager.facebookAuthColl.update(MongoDBObject("user_id" -> user_id), $set("access_token" -> access_token))
    MongoManager.usersColl.update(MongoDBObject("facebookUser.user_id" -> user_id), $set("facebookUser.access_token" -> access_token))
  }
}