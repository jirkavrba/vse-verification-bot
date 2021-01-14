package dev.vrba.verification

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

import java.awt.Color

class VerificationListener(private val configuration: Configuration) extends ListenerAdapter {

  private val service = new VerificationService(configuration.portalApiKey)

  override def onGuildMessageReceived(event: GuildMessageReceivedEvent): Unit = {
    // Only handle requests in verification channels
    if (event.getAuthor.isBot || !isInVerificationChannel(event)) return

    val pattern = """^\+verify ([0-9a-f]{32})$""".r

    event.getMessage.getContentDisplay match {
      case pattern(code) => verify(code, event)
      case _ => // In other cases do nothing
    }
  }

  private def isInVerificationChannel(event: GuildMessageReceivedEvent): Boolean =
    configuration.guilds
      .find(event.getGuild.getIdLong == _.guildId) match {
      case Some(guild) => event.getChannel.getIdLong == guild.channelId
      case None => false
    }

  private def verify(code: String, event: GuildMessageReceivedEvent): Unit = {
    event.getMessage.delete().queue()

    val verified = service.verify(code)
    val embed = new EmbedBuilder()
      .setAuthor(event.getAuthor.getName, null, event.getAuthor.getAvatarUrl)
      .setTimestamp(event.getMessage.getTimeCreated)

    if (verified) {
      embed.setColor(new Color(0x28a745))
      embed.setTitle("Verifikace proběhla úspěšně.")
      embed.setDescription("Během chvíle ti bude odemknut plný přístup na server.")

      assignVerifiedRoleToUser(event)
    }
    else {
      embed.setColor(new Color(0xdc3545))
      embed.setTitle("Verifikace selhala.")
      embed.setDescription("Pokud si myslíš že se jedná o chybu, kontaktuj někoho z moderátorů.")
    }

    event.getChannel.sendMessage(embed.build()).queue()
  }

  private def assignVerifiedRoleToUser(event: GuildMessageReceivedEvent): Unit = {
    // Calling .get is safe here, as the guild presence is guaranteed by event handle trigger
    val guild = event.getGuild
    val guildConfiguration = configuration.guilds.find(_.guildId == guild.getIdLong).get

    val member = guild.retrieveMember(event.getAuthor, false).complete
    val role = guild.getRoleById(guildConfiguration.verifiedRoleId)

    guild.addRoleToMember(member, role).queue()
  }
}
