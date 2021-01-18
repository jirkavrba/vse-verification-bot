package dev.vrba.verification


case class GuildConfiguration(
   guildId: Long,
   verificationChannelId: Long,
   logChannelId: Long,
   verifiedRoleId: Long
)

case class Configuration(
  discordToken: String,
  portalApiKey: String,
  guilds: List[GuildConfiguration]
)
