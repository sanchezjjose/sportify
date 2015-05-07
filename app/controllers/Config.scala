package controllers

import play.api.Play.current

trait Config {
  val config = play.api.Play.configuration
}

object Config extends Config {
  val msg = config.getString("msg").getOrElse("We're good to go. Let's get this W!")
  val emailMsg = config.getString("email-msg").getOrElse("Are you ready!? Let's get this W!")
  val mongoUrl = config.getString("mongo_url").get
  val dbName = config.getString("db_name").get
  val environment = config.getString("environment").get
  val fbAppId = config.getString("facebook_app_id").get
  val fbAppSecret = config.getString("facebook_app_secret").get
  val sendGridUsername = config.getString("sendgrid_username").get
  val sendGridPassword = config.getString("sendgrid_password").get
  val fromEmail = config.getString("from_email").get
  val testEmail = config.getString("test_email").get
}

object Environment {
  val DEVELOPMENT = "development"
  val PRODUCTION = "production"
}