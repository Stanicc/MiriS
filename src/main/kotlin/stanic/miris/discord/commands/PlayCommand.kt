package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerPlayCommand() {
    command("play") { runPlayCommand() }
    command("p") { runPlayCommand() }
    command("tocar") { runPlayCommand() }
}

private suspend fun CommandExecutor.runPlayCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query or link)") }
    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }

    var query = "ytsearch: "
    for (content in args.indices) query += "${args[content]} "

    val voiceChannel = member.voiceState!!.channel
    guild.audioManager.openAudioConnection(voiceChannel)

    val musicManager = getMusicManager()
    musicManager.load(query, member, channel)
}