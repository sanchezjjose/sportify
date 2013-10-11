package controllers

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import dao.SalatDAO
import com.mongodb.casbah.{MongoClient, MongoDB, MongoConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._


object MongoManager {
  val mongoConn = MongoClient(new MongoClientURI("mongodb://***REMOVED***:***REMOVED***@***REMOVED***:***REMOVED***/***REMOVED***"))
  val usersColl = mongoConn("***REMOVED***")("users")
  val gamesColl = mongoConn("***REMOVED***")("games")
  val facebookAuthColl = mongoConn("***REMOVED***")("facebook_autherizations")
}
