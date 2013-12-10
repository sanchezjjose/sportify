package controllers

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import dao.SalatDAO
import com.mongodb.casbah.{MongoClient, MongoDB, MongoConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._


object MongoManager extends Config {
  val mongoConn = MongoClient(new MongoClientURI(Config.mongoUrl))
  val usersColl = mongoConn("***REMOVED***")("users")
  val gamesColl = mongoConn("***REMOVED***")("games")
  val facebookAuthColl = mongoConn("***REMOVED***")("facebook_autherizations")
  val emailMessagesColl = mongoConn("***REMOVED***")("email_messages")

  try {
    emailMessagesColl.ensureIndex(MongoDBObject("game_id" -> 1, "recipient" -> 1), "game_id_", true)
  } catch {
    case e => println(e)
  }
}
