package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerJoinCommand() {
    command("join") { runJoinCommand() }
    command("entrar") { runJoinCommand() }
}

private suspend fun CommandExecutor.runJoinCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }

    val voiceChannel = member.voiceState!!.channel
    guild.audioManager.openAudioConnection(voiceChannel)

    channel.replyDeleting("<a:correct:807028817768087553> | I entered the voice channel that you are")
}