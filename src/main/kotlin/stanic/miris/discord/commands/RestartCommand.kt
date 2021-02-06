package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerRestartCommand() {
    command("restart") { runRestartCommand() }
    command("reset") { runRestartCommand() }
    command("resetar") { runRestartCommand() }
    command("recomecar") { runRestartCommand() }
}

private suspend fun CommandExecutor.runRestartCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }

    getMusicManager().getGuildPlayer(guild).playingTrack.position = 0
    channel.replyDeleting("<a:correct:807028817768087553> | Track restarted")
}