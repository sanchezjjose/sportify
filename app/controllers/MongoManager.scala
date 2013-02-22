package controllers

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import dao.SalatDAO
import com.mongodb.casbah.{MongoClient, MongoDB, MongoConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

/**
 * Created with IntelliJ IDEA.
 * User: jsanchez
 * Date: 2/19/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */

object MongoManager {
  val mongoConn = MongoClient("localhost",27017)
  val collection = mongoConn("sports")("users")
}
