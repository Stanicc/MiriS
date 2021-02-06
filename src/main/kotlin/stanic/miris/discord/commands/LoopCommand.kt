package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerLoopCommand() {
    command("loop") { runLoopCommand() }
    command("repetir") { runLoopCommand() }
}

private suspend fun CommandExecutor.runLoopCommand() {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    if (getMusicManager().getGuildPlayer(guild).isPaused) fail { channel.replyDeleting(":x: | The player is paused, resume it and try again") }

    val musicManager = getMusicManager()
    val trackModel = musicManager.getGuildTrackScheduler(guild).findTrack(musicManager.getGuildPlayer(guild).playingTrack)!!

    trackModel.loop = !trackModel.loop
    channel.replyDeleting("<:yessir:807416842011410532> | Loop ${if (trackModel.loop) "enabled" else "disabled"} for this track")
}