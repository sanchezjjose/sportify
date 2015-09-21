package util

import scala.util.Random


object Helper {

  def generateRandomId(): Long = {
    100000 + Random.nextInt(900000)
  }
}
