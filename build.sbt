name := """PlayReactiveMongoPolymer"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play24",
  "org.specs2" %% "specs2-core" % "2.4.9" % "test",
  "org.specs2" %% "specs2-junit" % "2.4.9" % "test",
  "javax.activation" % "activation" % "1.1",
  "javax.mail" % "mail" % "1.4",
  "com.google.oauth-client" % "google-oauth-client" % "1.17.0-rc",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.sendgrid" % "sendgrid-java" % "2.2.0"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

PlayKeys.fileWatchService := play.runsupport.FileWatchService.sbt(2000)

fork in run := true
