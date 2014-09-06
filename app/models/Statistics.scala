package models

//import java.net._
//
//import com.google.gdata.client.spreadsheet._
//import com.google.gdata.data.spreadsheet._
//
//import scala.collection.JavaConversions._
//import scala.math.BigDecimal.RoundingMode


/**
 * This should eventually be linked to a player, and season.
 * It will mean tracking historical data, per player and season.
 * It will also lend itself to finally be able to view data per game,
 * and eventually player profiles.
 */
case class PlayerStats(name: String,
                       gamesPlayed: String,
                       pointsPerGame: String,
                       assistsPerGame: String,
                       reboundsPerGame: String)

//object Statistics {
//
//  private[models] val service = new SpreadsheetService("SpreadsheetService-v1")
//
//  private[models] val cellFeedUrl = new URL("https://spreadsheets.google.com/feeds/cells/" +
//    "0AplJrXBnbh50dHRRVnB6SE5NMC1DWDgzMXY3SEthRnc/1/public/basic" +
//    "?min-col=1&max-col=5&min-row=2&max-row=14")
//
//  /**
//   * Temporary, disgusting and horrendous implementation just for initial try-out of this feature.
//   * @return
//   */
//  def pullStats : Iterator[(Player, PlayerStats)] = {
//
//    val cellFeed = service.getFeed(cellFeedUrl, classOf[CellFeed])
//
//    val stats = Player.findAll.flatMap { player =>
//
//      val name = PlayerHelper.formatName(player.firstName, player.lastName)
//      var index = 0
//
//      cellFeed.getEntries.map { cell =>
//
//        val statsOpt = if (name == cell.getPlainTextContent) {
//          val pointsPerGame = cellFeed.getEntries.get(index + 1).getPlainTextContent
//          val assistsPerGame = cellFeed.getEntries.get(index + 2).getPlainTextContent
//          val reboundsPerGame = cellFeed.getEntries.get(index + 3).getPlainTextContent
//          val gamesPlayed = cellFeed.getEntries.get(index + 4).getPlainTextContent
//
//          // Rounding
//          val ppg = BigDecimal(pointsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
//          val apg = BigDecimal(assistsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
//          val rpg = BigDecimal(reboundsPerGame).setScale(1, RoundingMode.CEILING).toDouble.toString
//
//          (player, Some(PlayerStats(name, gamesPlayed, ppg, apg, rpg)))
//        } else {
//          (player, None)
//        }
//
//        index = index + 1
//
//        statsOpt
//      }
//    }
//
//    stats.filter(_._2.isDefined).map(s => (s._1, s._2.get))
//  }
//}