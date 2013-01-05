package models

import scala.io.Source

object HelloWorld {

    def main(args: Array[String]): Unit = {
      for (line <- Source.fromFile("/web/svc-gilt-sports/app/resources/roster.txt").getLines()) {
        println("name: "+ line)
      }
    }

 }