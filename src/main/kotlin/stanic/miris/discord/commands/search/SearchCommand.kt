package stanic.miris.discord.commands.search

import br.com.devsrsouza.jda.command.*
import br.com.devsrsouza.jda.command.utils.on
import club.minnced.jda.reactor.on
import deezer.model.search.TracksSearch
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import stanic.miris.Main
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.await
import stanic.miris.utils.getTime
import stanic.miris.utils.replyDeleting
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun CommandListDefinition.registerSearchCommand() {
    command("search") { runSearchCommand() }
    command("pesquisar") { runSearchCommand() }
    command("song") { runSearchCommand() }
    command("track") { runSearchCommand() }
    command("music") { runSearchCommand() }
    command("musica") { runSearchCommand() }
}

private val reactions = linkedMapOf(
    "1️⃣" to 0,
    "2️⃣" to 1,
    "3️⃣" to 2,
    "4️⃣" to 3,
    "5️⃣" to 4,
    "6️⃣" to 5,
    "7️⃣" to 6,
    "8️⃣" to 7,
    "9️⃣" to 8,
    "\uD83D\uDD1F" to 9
)
private suspend fun CommandExecutor.runSearchCommand() {
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query) \n\n▫ For a better search use the following format: **$label** Artist - Song title") }

    var query = ""
    for (content in args.indices) query += "${args[content]} "

    channel.sendTyping().queue()
    var searchResult = Main.INSTANCE.deezerClient.getTracksSearchResults(TracksSearch(query)).take(10)

    var canDispose = false

    var position: Int
    val searchList = StringBuilder()
    if (searchResult.isEmpty()) searchList.append("``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1")
    else {
        position = 1
        for (track in searchResult) searchList.append("``${position++}``. ${track.artist.name} - ${track.title} | [${track.album.title}] \n")
        searchList.append("\n\n If the one you want is not in the list, do a new search by clicking \uD83D\uDCA1")
    }

    val searchMessage = channel.sendMessage(
        EmbedBuilder()
            .setTitle("Search")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription("<:menu:781976446418812958> Here's the track list **-** Select the right one by clicking on the emote for her position \n\n$searchList")
            .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
            .build()
    ).await()

    onDispose {
        message.delete().queue()
        try {
            delay(20000)
            searchMessage.delete().queue()
        } catch (ignored: Exception) {}
    }

    setup {
        on<GuildMessageReceivedEvent>().asFlow()
            .filter { it.channel == channel }
            .launchIn(GlobalScope)
        on<GuildMessageReactionAddEvent>().asFlow()
            .filter { it.messageIdLong == searchMessage.idLong }
            .filterNot { reactions.containsKey(it.reactionEmote.name) || it.reactionEmote.name == "\uD83D\uDCA1" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }

    reactions.keys.take(searchResult.size).forEach { searchMessage.addReaction(it).queue() }
    searchMessage.addReaction("\uD83D\uDCA1").queue()

    searchList.clear()

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == searchMessage.idLong }
                .filter { !it.user.isBot }
                .filter { reactions.containsKey(it.reactionEmote.name) || it.reactionEmote.name == "\uD83D\uDCA1" }
                .awaitFirst()
        } ?: fail {
            canDispose = true
            try {
                searchMessage.delete().queue()
            } catch (ignored: Exception) {}
        }

        when (choice.reactionEmote.name) {
            "\uD83D\uDCA1" -> {
                val embed = EmbedBuilder()
                    .setTitle("New query")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("<:search:807337010338988083> Write in that text channel what you want me to search for. \n*Remember to add more details now*")
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()
                searchMessage.editMessage(embed).await()
                searchMessage.clearReactions().queue()

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
                                searchMessage.delete().queue()
                            } catch (ignored: Exception) {}
                        }
                    }
                }

                query = newQuery.message.contentRaw

                newQuery.message.delete().queue()
                searchList.clear()
                searchResult = Main.INSTANCE.deezerClient.getTracksSearchResults(TracksSearch(query)).take(10)
                if (searchResult.isEmpty()) searchList.append("``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1")
                else {
                    position = 1
                    for (track in searchResult) searchList.append("``${position++}``. ${track.artist.name} - ${track.title} | [${track.album.title}] \n")
                    searchList.append("\n\n If the one you want is not in the list, do a new search by clicking \uD83D\uDCA1")
                }

                searchMessage.editMessage(EmbedBuilder()
                    .setTitle("Search")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("<:menu:781976446418812958> Here's the track list **-** Select the right one by clicking on the emote for her position \n\n$searchList")
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()).await()
                reactions.keys.take(searchResult.size).forEach { searchMessage.addReaction(it).queue() }
                searchMessage.addReaction("\uD83D\uDCA1").queue()
            }
            else -> {
                try {
                    val selected = searchResult[reactions[choice.reactionEmote.name]!!]!!
                    val track = Main.INSTANCE.deezerClient.getTrack(selected.id)

                    searchMessage.editMessage(EmbedBuilder()
                        .setTitle("Search")
                        .setColor(LIGHT_PINK_COLOR)
                        .setDescription("<:menu:781976446418812958> Here's the track information \n\n__**Track information**__ \n\uD83C\uDFA4 **Artist:** ${track.artist.name} \n\uD83C\uDFA7 **Title:** ${track.title}  \n\uD83D\uDCDA **Album:** ${track.album.title} \n\n⚠ **Explicit:** ${if (track.hasExplicitLyrics) "Yes" else "No"}\n\n" +
                                "\uD83D\uDCC6 **Release date:** ${
                                    if (track.releaseDate != null) "${track.releaseDate.split("-")[2]}/${track.releaseDate.split("-")[1]}/${track.releaseDate.split("-")[0]}" else "Not found"
                                } \n⏳ **Duration:** ${getTime(track.duration)} \n${if (track.trackPosition == null) "" else "\n\uD83D\uDCCE **Position in album:** ${track.trackPosition}"}\n\uD83C\uDFC6 **Rank in Deezer:** ${track.rank}")
                        .setThumbnail(track.album.mediumCover.toExternalForm())
                        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                        .build()).await()
                    searchMessage.clearReactions().queue()
                } catch (ignored: ArrayIndexOutOfBoundsException) {}

                canDispose = true
            }
        }
    }
}