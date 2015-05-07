name := """sportify"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.8",
  "javax.activation" % "activation" % "1.1",
  "javax.mail" % "mail" % "1.4",
  "com.google.oauth-client" % "google-oauth-client" % "1.17.0-rc",
  "com.newrelic.agent.java" % "newrelic-agent" % "2.21.4",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.sendgrid" % "sendgrid-java" % "2.2.0"
)

libraryDependencies += ws
