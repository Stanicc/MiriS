package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerStopCommand() {
    command("stop") { runStopCommand() }
    command("parar") { runStopCommand() }
}

private suspend fun CommandExecutor.runStopCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }

    getMusicManager().reset(guild)
    channel.replyDeleting("<a:correct:807028817768087553> | Queue cleaned and player stopped")
}