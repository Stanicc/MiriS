package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.CommandHolder
import br.com.devsrsouza.jda.command.command
import br.com.devsrsouza.jda.command.fail
import br.com.devsrsouza.jda.command.onDispose
import kotlinx.coroutines.delay
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.replyDeleting

fun CommandHolder.registerVolumeCommand() = command("volume") {
    onDispose {
        delay(5000)
        message.delete().queue()
    }

    if (member.voiceState == null || !member.voiceState!!.inVoiceChannel()) fail { channel.replyDeleting(":x: | You must be on a voice channel to do that") }
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    if (getMusicManager().getGuildPlayer(guild).isPaused) fail { channel.replyDeleting(":x: | The player is paused, resume it and try again") }
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (number from 1 to 100)") }

    try {
        val previousVolume = getMusicManager().getGuildPlayer(guild).volume
        getMusicManager().getGuildPlayer(guild).volume = Integer.parseInt(args[0])
        channel.replyDeleting(if (Integer.parseInt(args[0]) > previousVolume) "<:up:807698886113361920> | Volume increased to ${Integer.parseInt(args[0])}" else "<:down:807698979461791805> | Volume decreased to ${Integer.parseInt(args[0])}")
    } catch (exception: NumberFormatException) { fail { channel.replyDeleting(":x: | Use **$label** (number from 1 to 100)") } }
}