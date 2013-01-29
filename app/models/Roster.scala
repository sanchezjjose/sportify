package models

import scala.io.Source

object Roster {
	def parseNames : Iterator[String] = {
		Source.fromFile("/web/svc-gilt-sports/app/resources/roster.txt").getLines()
	}
}