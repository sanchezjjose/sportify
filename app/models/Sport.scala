package models

import models.Sport.SportName.SportName


/**
 * Model for the currently supported sports.
 */
case class Sport(id: Long,
                 sportName: SportName)

object Sport {

  object SportName extends Enumeration {
    type SportName = Value

    val Basketball, Baseball, Softball = Value
  }
}
