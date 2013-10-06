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

  /**
   * Add new users FB information.
   *
   * @param facebookUser The facebook user
   */
  def insert(access_token: String, user_id: String) = {
  	val facebookUser = FacebookUser(access_token, user_id)
    val dbo = grater[FacebookUser].asDBObject(facebookUser)
    MongoManager.facebookAuthColl += dbo
  }
}