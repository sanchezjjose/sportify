package models

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

  // Generates Writes and Reads thanks to Json Macros
  implicit val accountFormat = Json.format[EmailMessage]
  implicit val gameFormat = Json.format[Game]
  implicit val playerFormat = Json.format[Player]
  implicit val seasonFormat = Json.format[Season]
  implicit val sportFormat = Json.format[Sport]
  implicit val teamFormat = Json.format[Team]
  implicit val userFormat = Json.format[User]
}