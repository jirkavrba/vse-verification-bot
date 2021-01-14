package dev.vrba.verification

import io.circe.generic.auto._
import io.circe.parser._
import net.dv8tion.jda.api.JDABuilder

import java.io.File
import scala.io.Source

object Bot {
  def main(args: Array[String]): Unit = {
    loadConfigurationFromJson() match {
      case Some(configuration) =>
        JDABuilder.createDefault(configuration.discordToken)
          .addEventListeners(new VerificationListener(configuration))
          .build()
          .awaitReady()

      case None => println(
        """Invalid bot configuration!
          |
          |Please make sure the file config.json exists in application runtime folder and
          |all values are filled correctly according to README.
          |""".stripMargin)
    }
  }

  private def loadConfigurationFromJson(): Option[Configuration] = {
    try {
      val source = Source.fromFile(new File("./config.json").getAbsolutePath)
      decode[Configuration](source.mkString) match {
        case Left(_) => None
        case Right(configuration) => Some(configuration)
      }
    }
    catch {
      case _: Throwable => None
    }
  }
}
