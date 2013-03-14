package models

import scala.io.Source

object Roster {
	def parseNames : Iterator[String] = {
		Source.fromFile("app/resources/roster.txt").getLines()
	}
}