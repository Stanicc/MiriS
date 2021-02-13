package stanic.miris.discord.commands.music

import br.com.devsrsouza.jda.command.CommandExecutor
import br.com.devsrsouza.jda.command.CommandListDefinition
import br.com.devsrsouza.jda.command.command
import br.com.devsrsouza.jda.command.fail
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.reply
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerPauseCommand() {
    command("pause") { runPauseCommand() }
    command("pausar") { runPauseCommand() }
}

private suspend fun CommandExecutor.runPauseCommand() {
    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    if (getMusicManager().getGuildPlayer(guild).isPaused) fail { channel.replyDeleting(":x: | The player is already paused") }

    getMusicManager().getGuildPlayer(guild).isPaused = true
    channel.reply("<:yuqicry:807032574018191365> | Player paused")
}