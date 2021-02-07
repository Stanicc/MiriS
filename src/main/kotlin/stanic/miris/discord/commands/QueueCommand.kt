package stanic.miris.discord.commands

import br.com.devsrsouza.jda.command.*
import br.com.devsrsouza.jda.command.utils.on
import club.minnced.jda.reactor.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import stanic.miris.Main
import stanic.miris.manager.getMusicManager
import stanic.miris.music.model.TrackModel
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.await
import stanic.miris.utils.getTime
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerQueueCommand() {
    command("queue") { runQueueCommand() }
    command("fila") { runQueueCommand() }
}

private suspend fun CommandExecutor.runQueueCommand() {
    if (getMusicManager().getGuildPlayer(guild).playingTrack == null) fail { channel.replyDeleting(":x: | I'm not playing anything right now") }
    val trackScheduler = getMusicManager().getGuildTrackScheduler(guild)
    var playingNow = trackScheduler.findTrack(getMusicManager().getGuildPlayer(guild).playingTrack)!!

    val tracks = ArrayList<TrackModel>(trackScheduler.queueList)
    tracks.remove(playingNow)

    var currentPage = 1
    val size = tracks.size
    var canDispose = false

    val queueList = StringBuilder()
    if (tracks.isEmpty()) queueList.append("- `The queue is empty`")
    else for (i in 0..10) {
        if (i + (10 * (currentPage - 1)) < size) queueList.append("`[${getTime(tracks[i + (10 * (currentPage - 1))].track.duration)}]` - ${tracks[i + (10 * (currentPage - 1))].track.info.title} \n")
    }
    val queueMessage = channel.sendMessage(EmbedBuilder()
        .setTitle("Queue")
        .setColor(LIGHT_PINK_COLOR)
        .setDescription("<:menu:781976446418812958> Here's the track list **-** Page $currentPage \n\n\uD83C\uDFA7 **Playing now**: ${playingNow.track.info.title} \n⏳ **Time left:** ${getTime(System.currentTimeMillis() - playingNow.startedTime)} \n\uD83D\uDCCC **Requested by:** ${playingNow.member.asMention} \n\n\n$queueList")
        .build()).await()
    if (tracks.isNotEmpty()) {
        queueMessage.addReaction("◀").queue()
        queueMessage.addReaction("⏭").queue()
    } else canDispose = true

    queueList.clear()

    onDispose {
        delay(20000)
        queueMessage.delete().queue()
        message.delete().queue()
    }
    setup {
        on<GuildMessageReactionAddEvent>().asFlow()
            .filter { it.messageIdLong == queueMessage.idLong }
            .filterNot { it.reactionEmote.name == "⏭" || it.reactionEmote.name == "◀" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == queueMessage.idLong }
                .filter { !it.user.isBot }
                .filter { it.reactionEmote.name == "⏭" || it.reactionEmote.name == "◀" }
                .awaitFirst()
        } ?: fail {
            canDispose = true
            queueMessage.clearReactions().queue()
        }

        when (choice.reactionEmote.name) {
            "⏭" -> currentPage += 1
            "◀" -> if (currentPage == 1) currentPage = 1 else currentPage -= 1
        }
        choice.reaction.removeReaction(member.user).queue()

        if (trackScheduler.findTrack(getMusicManager().getGuildPlayer(guild).playingTrack) != null && trackScheduler.findTrack(getMusicManager().getGuildPlayer(guild).playingTrack) != null) playingNow = trackScheduler.findTrack(getMusicManager().getGuildPlayer(guild).playingTrack)!!

        for (i in 0..10) {
            if (i + (10 * (currentPage - 1)) < size) queueList.append("`[${getTime(tracks[i + (10 * (currentPage - 1))].track.duration)}]` - ${tracks[i + (10 * (currentPage - 1))].track.info.title} \n")
        }

        if (queueList.isEmpty()) {
            currentPage = 1
            queueList.clear()
            for (i in 0..10) {
                if (i + (10 * (currentPage - 1)) < size) queueList.append("`[${getTime(tracks[i + (10 * (currentPage - 1))].track.duration)}]` - ${tracks[i + (10 * (currentPage - 1))].track.info.title} \n")
            }
        }

        queueMessage.editMessage(EmbedBuilder()
            .setTitle("Queue")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription("<:menu:781976446418812958> Here's the track list **-** Page $currentPage \n\n\uD83C\uDFA7 **Playing now**: ${playingNow.track.info.title} \n⏳ **Time left:** ${getTime(System.currentTimeMillis() - playingNow.startedTime)} \n\uD83D\uDCCC **Requested by:** ${playingNow.member.asMention} \n\n\n$queueList")
            .build()).queue()

        queueList.clear()
    }
}