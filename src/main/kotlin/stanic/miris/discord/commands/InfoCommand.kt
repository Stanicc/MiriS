package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.getTime
import stanic.miris.utils.reply
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerInfoCommand() {
    command("info") { runInfoCommand() }
    command("playing") { runInfoCommand() }
    command("playingnow") { runInfoCommand() }
    command("nowplaying") { runInfoCommand() }
    command("track") { runInfoCommand() }
    command("music") { runInfoCommand() }
    command("tocando") { runInfoCommand() }
    command("tocandoagora") { runInfoCommand() }
    command("agoratocando") { runInfoCommand() }
    command("musica") { runInfoCommand() }
}

private suspend fun CommandExecutor.runInfoCommand() {
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null)
        fail { channel.replyDeleting(":x: | I'm not playing anything right now") }

    val musicManager = getMusicManager()
    val trackModel = musicManager.getGuildTrackScheduler(guild).findTrack(musicManager.getGuildPlayer(guild).playingTrack)!!
    val playingTrack = trackModel.track

    val embed = EmbedBuilder()
        .setTitle("Playing info")
        .setColor(LIGHT_PINK_COLOR)
        .setDescription("<:menu:781976446418812958> Here's the playing info \n\n\uD83C\uDFA7 **Playing now**: ${playingTrack.info.title} \n⏳ **Duration:** ${getTime(playingTrack.duration)} \n     - **Time left:** ${getTime(System.currentTimeMillis() - trackModel.startedTime)} \n<:youtube:807338481990500462> **Channel:** [${playingTrack.info.author}](${playingTrack.info.uri}) \n\uD83D\uDC4C **Requested by:** ${member.nickname ?: member.user.name} \n\n\uD83D\uDD09 Volume: ${getMusicManager().getGuildPlayer(guild).volume} \n\uD83D\uDCE2 Bass mode: ${musicManager.getGuildTrackScheduler(guild).bassMode.getName()} \n${if (trackModel.loop) "\n__This song is in a loop__" else ""}")
        .setThumbnail("https://i3.ytimg.com/vi/${playingTrack.info.identifier}/maxresdefault.jpg")
        .build()
    channel.reply(embed) {
        GlobalScope.launch {
            delay(20000)

            delete().queue()
            message.delete().queue()
        }
    }
}