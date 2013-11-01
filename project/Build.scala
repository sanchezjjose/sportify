import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "sportify"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.novus" %% "salat" % "1.9.2",
      "javax.activation" % "activation" % "1.1",
      "javax.mail" % "mail" % "1.4"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"      
    )

}
