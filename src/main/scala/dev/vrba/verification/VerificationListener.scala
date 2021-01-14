package dev.vrba.verification

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VerificationListener(private val configuration: Configuration) extends ListenerAdapter {

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    // Only handle requests in verification channels
    if (event.getAuthor.isBot || !isInVerificationChannel(event)) return

    val pattern = """^\+verify ([0-9a-f]{32})$""".r

    event.getMessage.getContentDisplay match {
      case pattern(code) => verify(code)
      case _ => // In other cases do nothing
    }
  }

  private def isInVerificationChannel(event: GuildMessageReceivedEvent): Boolean =
    configuration.guilds
      .find(event.getGuild.getIdLong == _.guildId) match {
      case Some(guild) => event.getChannel.getIdLong == guild.channelId
      case None => false
    }

  private def verify(code: String): Unit = {
    println(s"Verifying code [$code]")
  }
}
