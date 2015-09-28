package models


case class Sport (
  _id: Long,
  name: String,
  logo_image_url: String
)

object Sport {

  object Name extends Enumeration {
    type Name = Value

    val Basketball, Softball, Soccer, Football, Ping_Pong = Value
  }
}

object SportFields {
  val Id = "_id"
  val Name = "name"
  val LogoImageUrl = "logo_image_url"
}
