package stanic.miris.discord.commands.misc

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
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import stanic.miris.Main
import stanic.miris.downloader.YoutubeDownloaderService
import stanic.miris.downloader.callback.DownloaderCallback
import stanic.miris.manager.getMusicManager
import stanic.miris.utils.*
import stanic.miris.utils.bot.*
import java.io.File

fun CommandListDefinition.registerDownloadCommand() {
    command("download") { runDownloadCommand() }
    command("baixar") { runDownloadCommand() }
}

private suspend fun CommandExecutor.runDownloadCommand() {
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query or link)") }

    val musicManager = getMusicManager()
    var query = ""
    for (content in args.indices) query += "${args[content]} "

    var loadedTrack = musicManager.load(query, member, channel, true) ?: fail {}
    var canDispose = false

    var loadedMessage = channel.sendMessage(EmbedBuilder()
        .setTitle("Download")
        .setColor(LIGHT_PINK_COLOR)
        .setDescription("<:search:807337010338988083> Here's the download information \n\n__**Information**__ \n\uD83C\uDFA7 **Title:** ${loadedTrack.track.info.title} \n⏳ **Duration:** ${getTime(loadedTrack.track.duration)} \n<:youtube:807338481990500462> **Youtube link:** [click here](${loadedTrack.track.info.uri}) \n\n\uD83D\uDCCC **Your search:** ${query.replace("ytsearch: ", "")} \n\n\n**Click on the emote for the format to download** \n  __-__ **Audio (MP3):** ${NUMBER_ONE.asMention} \n   __-__ **Video (MP4):** ${NUMBER_TWO.asMention} \n\nIf you want to do a new search click on \uD83D\uDCA1")
        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
        .build()).await()
    loadedMessage.addReaction(NUMBER_ONE).queue()
    loadedMessage.addReaction(NUMBER_TWO).queue()
    loadedMessage.addReaction("\uD83D\uDCA1").queue()

    setup {
        on<GuildMessageReceivedEvent>().asFlow()
            .filter { it.channel == channel }
            .launchIn(GlobalScope)
        on<GuildMessageReactionAddEvent>().asFlow()
            .filter { it.messageIdLong == loadedMessage.idLong }
            .filterNot { it.reactionEmote.name == NUMBER_ONE.name || it.reactionEmote.name == NUMBER_TWO.name || it.reactionEmote.name == "\uD83D\uDCA1" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == loadedMessage.idLong }
                .filter { !it.user.isBot }
                .filter { it.reactionEmote.name == NUMBER_ONE.name || it.reactionEmote.name == NUMBER_TWO.name || it.reactionEmote.name == "\uD83D\uDCA1" }
                .awaitFirst()
        } ?: fail {
            if (!canDispose) {
                canDispose = true
                loadedMessage.clearReactions().queue()
                loadedMessage.delete().queue()
            }
        }

        if (choice.reactionEmote.name == "\uD83D\uDCA1") {

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
                            message.delete().queue()
                        } catch (ignored: Exception) {}
                    }
                }
            }

            query = newQuery.message.contentRaw
            loadedTrack = musicManager.load(query, member, channel, true) ?: fail {}

            newQuery.message.delete().queue()
            loadedMessage.editMessage(EmbedBuilder()
                .setTitle("Download")
                .setColor(LIGHT_PINK_COLOR)
                .setDescription("<:search:807337010338988083> Here's the download information \n\n__**Information**__ \n\uD83C\uDFA7 **Title:** ${loadedTrack.track.info.title} \n⏳ **Duration:** ${getTime(loadedTrack.track.duration)} \n<:youtube:807338481990500462> **Youtube link:** [click here](${loadedTrack.track.info.uri}) \n\n\uD83D\uDCCC **Your search:** ${query.replace("ytsearch: ", "")} \n\n\n**Click on the emote for the format to download** \n  __-__ **Audio (MP3):** ${NUMBER_ONE.asMention} \n   __-__ **Video (MP4):** ${NUMBER_TWO.asMention} \n\nIf you want to do a new search click on \uD83D\uDCA1")
                .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                .build()).queue()
            loadedMessage.addReaction(NUMBER_ONE).queue()
            loadedMessage.addReaction(NUMBER_TWO).queue()
            loadedMessage.addReaction("\uD83D\uDCA1").queue()
        } else {
            loadedMessage.delete().queue()
            loadedMessage = channel.sendMessage("<a:loading:834462333337993226> | Downloading...").complete()

            YoutubeDownloaderService().download(loadedTrack.track.info.identifier, choice.reactionEmote.name == NUMBER_ONE.name, object : DownloaderCallback {
                override fun onError(throwable: Throwable) {
                    loadedMessage.editMessage(":x: | An error as occurred (${throwable.cause.toString()})").queue()
                }

                override fun onFinish(file: File) {
                    canDispose = true

                    loadedMessage.delete().queue()
                    loadedMessage = channel.sendMessage("<a:correct:807028817768087553> | Download complete!")
                        .addFile(file)
                        .complete()

                    onDispose {
                        delay(120000)
                        loadedMessage.delete().queue()
                        message.delete().queue()
                        file.delete()
                    }
                }
            })
        }
    }
}