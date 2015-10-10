package models

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

  // Generates Writes and Reads thanks to Json Macros
  implicit val emailMessageFormat = Json.format[EmailMessage]
  implicit val gameFormat = Json.format[Game]
  implicit val playerFormat = Json.format[Player]
  implicit val seasonFormat = Json.format[Season]
  implicit val sportFormat = Json.format[Sport]
  implicit val teamFormat = Json.format[Team]
  implicit val userFormat = Json.format[User]

  implicit val teamViewModelFormat = Json.format[TeamViewModel]
  implicit val accountViewModelFormat = Json.format[AccountViewModel]
  implicit val playerViewModelFormat = Json.format[PlayerViewModel]
  implicit val homeViewModelFormat = Json.format[HomepageViewModel]
  implicit val rosterViewModelFormat = Json.format[RosterViewModel]
  implicit val scheduleViewModelFormat = Json.format[ScheduleViewModel]
}