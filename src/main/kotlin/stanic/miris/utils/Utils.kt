package stanic.miris.utils

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel

fun TextChannel.reply(message: String) = sendMessage(message).complete()
fun TextChannel.reply(embed: MessageEmbed) = sendMessage(embed).complete()

fun Member.reply(message: String) = user.openPrivateChannel().complete().sendMessage(message).complete()
fun Member.reply(embed: MessageEmbed) = user.openPrivateChannel().complete().sendMessage(embed).complete()