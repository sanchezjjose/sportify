package com.sportify.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.sportify.config.Config
import utils.Loggable


class MongoManager(dbName: String, mongoUrl: String) extends Loggable {

  val mongoConn = MongoClient(new MongoClientURI(Config.mongoUrl))

  // Collections
  lazy val users = mongoConn(dbName)("users")
  lazy val facebookAuths = mongoConn(dbName)("facebook_authorizations")
  lazy val games = mongoConn(dbName)("games")
  lazy val seasons = mongoConn(dbName)("seasons")
  lazy val teams = mongoConn(dbName)("teams")
  lazy val emailMessages = mongoConn(dbName)("email_messages")

  try {
    emailMessages.ensureIndex(MongoDBObject("game_id" -> 1, "recipient" -> 1), "game_id_", true)
  } catch {
    case e: Exception => log.error("There was an error creating indexes for the email messages collection.", e)
  }
}

object MongoManagerFactory extends Loggable {

  def instance: MongoManager = new MongoManager(Config.dbName, Config.mongoUrl)
}
