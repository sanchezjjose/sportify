package controllers

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject


object MongoManager extends Config with Loggable {

  // Setup
  val mongoConn = MongoClient(new MongoClientURI(Config.mongoUrl))
  val dbName = "app18602579"

  // Collections
  val users = mongoConn(dbName)("users")
  val facebookAuths = mongoConn(dbName)("facebook_autherizations")
  val games = mongoConn(dbName)("games")
  val seasons = mongoConn(dbName)("seasons")
  val teams = mongoConn(dbName)("teams")
  val emailMessages = mongoConn(dbName)("email_messages")

  try {
    emailMessages.ensureIndex(MongoDBObject("game_id" -> 1, "recipient" -> 1), "game_id_", true)
  } catch {
    case e: Exception => log.error("There was an error creating indexes for the email messages collection.", e)
  }
}
