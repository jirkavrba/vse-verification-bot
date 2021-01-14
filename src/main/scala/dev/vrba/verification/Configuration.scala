package dev.vrba.verification


case class GuildConfiguration(
   guildId: Long,
   channelId: Long,
   verifiedRoleId: Long
)

case class Configuration(
  discordToken: String,
  portalApiKey: String,
  guilds: List[GuildConfiguration]
)
