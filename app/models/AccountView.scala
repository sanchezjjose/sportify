package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}


case class AccountView(teams: TeamViewModel,
                       playerId: Long,
                       email: String,
                       password: Option[String],
                       firstName: String,
                       lastName: String,
                       number: Int,
                       phoneNumber: Option[String],
                       position: Option[String],
                       isAdmin: Boolean)

object AccountView {

  implicit val writes: Writes[AccountView] = (
      (JsPath \ "teams").write[TeamViewModel] and
      (JsPath \ "player_id").write[Long] and
      (JsPath \ "email").write[String] and
      (JsPath \ "password").write[Option[String]] and
      (JsPath \ "first_name").write[String] and
      (JsPath \ "last_name").write[String] and
      (JsPath \ "number").write[Int] and
      (JsPath \ "phone_number").write[Option[String]] and
      (JsPath \ "position").write[Option[String]] and
      (JsPath \ "is_admin").write[Boolean]
    )(unlift(AccountView.unapply))
}
