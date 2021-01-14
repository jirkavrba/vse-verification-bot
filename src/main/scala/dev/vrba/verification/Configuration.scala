package dev.vrba.verification


case class GuildConfiguration(guildId: String, verificationChannelId: Long)

case class Configuration(
  discordToken: String,
  portalApiKey: String,
  guilds: List[GuildConfiguration]
)
