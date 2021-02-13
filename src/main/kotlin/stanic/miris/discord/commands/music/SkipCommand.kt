package stanic.miris.discord.commands.music

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerSkipCommand() {
    command("skip") { runSkipCommand() }
    command("pular") { runSkipCommand() }
    command("next") { runSkipCommand() }
}

private suspend fun CommandExecutor.runSkipCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    if (getMusicManager().getGuildPlayer(guild).isPaused) fail { channel.replyDeleting(":x: | The player is paused, resume it and try again") }

    val musicManager = getMusicManager()
    musicManager.getGuildPlayer(guild).stopTrack()

    if (musicManager.getGuildPlayer(guild).playingTrack != null) channel.replyDeleting("<a:correct:807028817768087553> | Track skipped")
}