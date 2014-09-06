package models

import scala.util.Random

trait Helper {

  def generateRandomId(): Long = {
    100000 + Random.nextInt(900000)
  }
}
