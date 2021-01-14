package dev.vrba.verification

import net.dv8tion.jda.api.JDABuilder

object Bot {
  def main(args: Array[String]): Unit = {
    val client = JDABuilder.createDefault("token")
      .build()
      .awaitReady()
  }
}
