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
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import stanic.miris.Main
import stanic.miris.manager.getMusicManager
import stanic.miris.music.model.TrackModel
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.await
import stanic.miris.utils.getTime
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

    if (guild.audioManager.connectedChannel == null) {
        val voiceChannel = member.voiceState!!.channel
        guild.audioManager.openAudioConnection(voiceChannel)
    }

    val musicManager = getMusicManager()

    if (query.replaceFirst("ytsearch: ", "").startsWith("http")) {
        musicManager.load(query, member, channel)
        return
    }

    var loadedTrack = musicManager.loadWaiting(query, member, channel) ?: fail {}
    var canDispose = false

    val loadedMessage = channel.sendMessage(EmbedBuilder()
        .setTitle("Confirm")
        .setColor(LIGHT_PINK_COLOR)
        .setDescription("<:search:807337010338988083> I searched for what you said, is that what you were looking for? \n\n__**Information**__ \n\uD83C\uDFA7 **Title:** ${loadedTrack.track.info.title} \n⏳ **Duration:** ${getTime(loadedTrack.track.duration)} \n<:youtube:807338481990500462> **Youtube link:** [click here](${loadedTrack.track.info.uri}) \n\n\uD83D\uDCCC **Your search:** ${query.replace("ytsearch: ", "")} \n\n\nIf so, click ✅ to ${if (musicManager.getGuildPlayer(guild).playingTrack == null) "add to the queue" else "play"}. If not click on ❌ to do a new search")
        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
        .build()).await()
    loadedMessage.addReaction("✅").queue()
    loadedMessage.addReaction("❌").queue()

    setup {
        on<GuildMessageReceivedEvent>().asFlow()
            .filter { it.channel == channel }
            .launchIn(GlobalScope)
        on<GuildMessageReactionAddEvent>().asFlow()
            .filter { it.messageIdLong == loadedMessage.idLong }
            .filterNot { it.reactionEmote.name == "✅" || it.reactionEmote.name == "❌" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == loadedMessage.idLong }
                .filter { !it.user.isBot }
                .filter { it.reactionEmote.name == "✅" || it.reactionEmote.name == "❌" }
                .awaitFirst()
        } ?: fail {
            canDispose = true
            loadedMessage.clearReactions().queue()
            loadedMessage.delete().queue()
        }

        when (choice.reactionEmote.name) {
            "✅" -> {
                musicManager.getGuildTrackScheduler(guild).queue(loadedTrack.track, member, channel)
                try {
                    loadedMessage.delete().queue()
                } catch (ignored: Exception) {}
                canDispose = true
            }
            "❌" -> {
                val embed = EmbedBuilder()
                    .setTitle("New query")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("<:search:807337010338988083> Write in that text channel what you want me to search for. \n*Remember to add more details now*")
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()
                loadedMessage.editMessage(embed).await()
                loadedMessage.clearReactions().queue()

                val newQuery = withTimeoutOrNull(1200000) {
                    Main.INSTANCE.manager.on<GuildMessageReceivedEvent>()
                        .filter { it.channel == channel }
                        .filter { it.author.id == member.id }
                        .awaitFirst()
                } ?: fail {
                    channel.sendMessage(":x: | You took too long to write the new query.").queue {
                        GlobalScope.launch {
                            delay(5000)
                            it.delete().queue()
                            try {
                                loadedMessage.delete().queue()
                            } catch (ignored: Exception) {}
                        }
                    }
                }

                query = "ytsearch: ${newQuery.message.contentRaw}"
                loadedTrack = musicManager.loadWaiting(query, member, channel) ?: fail {}

                newQuery.message.delete().queue()

                loadedMessage.editMessage(EmbedBuilder()
                    .setTitle("Confirm")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("<:search:807337010338988083> I searched for what you said, is that what you were looking for? \n\n__**Information**__ \n\uD83C\uDFA7 **Title:** ${loadedTrack.track.info.title} \n⏳ **Duration:** ${getTime(loadedTrack.track.duration)} \n<:youtube:807338481990500462> **Youtube link:** [click here](${loadedTrack.track.info.uri}) \n\n\uD83D\uDCCC **Your search:** ${query.replace("ytsearch: ", "")} \n\n\nIf so, click ✅ to ${if (musicManager.getGuildPlayer(guild).playingTrack == null) "add to the queue" else "play"}. If not click on ❌ to do a new search")
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()).queue()
                loadedMessage.addReaction("✅").queue()
                loadedMessage.addReaction("❌").queue()
            }
        }
    }
}