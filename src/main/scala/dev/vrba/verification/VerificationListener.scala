package dev.vrba.verification

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

import java.awt.Color
import java.util.logging.Logger

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
      case Some(guild) => event.getChannel.getIdLong == guild.verificationChannelId
      case None => false
    }

  private def verify(code: String, event: GuildMessageReceivedEvent): Unit = {
    event.getMessage.delete().queue()

    val result = service.verify(code, event.getAuthor.getIdLong)

    val embed = new EmbedBuilder()
      .setAuthor(event.getAuthor.getName, null, event.getAuthor.getAvatarUrl)
      .setTimestamp(event.getMessage.getTimeCreated)

    result match {
      case Success =>
        embed.setColor(new Color(0x28a745))
        embed.setTitle("Verifikace proběhla úspěšně.")
        embed.setDescription("Během chvíle ti bude odemknut plný přístup na server.")

        assignVerifiedRoleToUser(event)

      case Failure(reason) =>
        embed.setColor(new Color(0xdc3545))
        embed.setTitle("Verifikace selhala.")
        embed.setDescription("Pokud si myslíš že se jedná o chybu, kontaktuj někoho z moderátorů.")

        embed.addField("Failure reason:", s"`$reason`", false)
    }

    event.getAuthor
      .openPrivateChannel()
      .complete()
      .sendMessage(embed.build())
      .queue()

    log(result, event)
  }

  private def assignVerifiedRoleToUser(event: GuildMessageReceivedEvent): Unit = {
    // Calling .get is safe here, as the guild presence is guaranteed by event handle trigger
    val guild = event.getGuild
    val guildConfiguration = configuration.guilds.find(_.guildId == guild.getIdLong).get

    val member = guild.retrieveMember(event.getAuthor, false).complete
    val role = guild.getRoleById(guildConfiguration.verifiedRoleId)

    guild.addRoleToMember(member, role).queue()
  }

  private def log(result: VerificationResult, event: GuildMessageReceivedEvent): Unit = {
    val guild = event.getGuild
    val guildConfiguration = configuration.guilds.find(_.guildId == guild.getIdLong).get


    Option(guild.getTextChannelById(guildConfiguration.logChannelId)) match {
      case Some(channel) =>
        val embed = new EmbedBuilder()
        val author = event.getAuthor

        embed.setAuthor(author.getName, null, author.getAvatarUrl)
        embed.setTimestamp(event.getMessage.getTimeCreated)
        embed.addField("Discord ID", author.getId, false)
        embed.addField("Mention", author.getAsMention, false)

        result match {
          case Success =>
            embed.setTitle("Verification successful")
            embed.setColor(new Color(0x28a745))

          case Failure(reason) =>
            embed.setTitle("Verification failed")
            embed.setColor(new Color(0xdc3545))
            embed.addField("Error", s"`$reason`", false)
        }

        channel.sendMessage(embed.build()).queue()

      case None => Logger
        .getAnonymousLogger
        .severe(s"Cannot find verification log channel for guild ${guild.getName}")
    }
  }
}
