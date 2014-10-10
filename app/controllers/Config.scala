package controllers

import play.api.Play.current

trait Config {
  val config = play.api.Play.configuration
}

object Config extends Config {
  lazy val msg = config.getString("msg").getOrElse("We're good to go. Let's get this W!")
  lazy val emailMsg = config.getString("email-msg").getOrElse("Are you ready!? Let's get this W!")
  lazy val mongoUrl = config.getString("mongo_url").get
  lazy val environment = config.getString("environment").get
  lazy val fbAppId = config.getString("facebook_app_id").get
  lazy val fbAppSecret = config.getString("facebook_app_secret").get
}

object Environment {
  val DEVELOPMENT = "development"
  val PRODUCTION = "production"
}