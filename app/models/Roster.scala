package models

import scala.io.Source
import com.google.gdata.client.authn.oauth._
import com.google.gdata.client.spreadsheet._
import com.google.gdata.data.spreadsheet._
import java.net._
import scala.collection.JavaConversions._
import scala.math.BigDecimal.RoundingMode

object Roster {

  private[models] val service = new SpreadsheetService("SpreadsheetService-v1")

  private[models] val cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/" +
    "0AplJrXBnbh50dHRRVnB6SE5NMC1DWDgzMXY3SEthRnc/1/public/basic" +
    "?min-col=1&max-col=4&min-row=2&max-row=14")

	def parseNames : Iterator[String] = {
		Source.fromFile("app/resources/roster.txt").getLines()
	}

  /**
   * Temporary, disgusting and horrendous implementation just for initial try-out of this feature.
   * @return
   */
  def pullStats : Iterator[(User, PlayerStats)] = {

    val cellFeed = service.getFeed(cellFeedUrl, classOf[CellFeed])

    val stats = User.findAll.flatMap { player =>

      val name = PlayerHelper.formatName(player.firstName, player.lastName)
      var index = 0

      cellFeed.getEntries.map { cell =>

        val statsOpt = if (name == cell.getPlainTextContent) {
          val pointsPerGame = cellFeed.getEntries.get(index + 1).getPlainTextContent
          val assistsPerGame = cellFeed.getEntries.get(index + 2).getPlainTextContent
          val reboundsPerGame = cellFeed.getEntries.get(index + 3).getPlainTextContent

          // Prettify
          val ppg = if (cellFeed.getEntries.get(index + 1).getPlainTextContent == "0") {
            pointsPerGame
          } else {
            BigDecimal(pointsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
          }
          val apg = if (cellFeed.getEntries.get(index + 2).getPlainTextContent == "0") {
            assistsPerGame
          } else {
            BigDecimal(assistsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
          }
          val rpg = if (cellFeed.getEntries.get(index + 3).getPlainTextContent == "0") {
            reboundsPerGame
          } else {
            BigDecimal(reboundsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
          }

          (player, Some(PlayerStats(name, ppg, apg, rpg)))
        } else {
          (player, None)
        }

        index = index + 1

        statsOpt
      }
    }

    stats.filter(_._2.isDefined).map(s => (s._1, s._2.get))
  }
}