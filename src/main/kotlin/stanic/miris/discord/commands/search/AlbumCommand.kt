package stanic.miris.discord.commands.search

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
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import stanic.miris.Main
import stanic.miris.utils.*

fun CommandListDefinition.registerAlbumCommand() {
    command("album") { runAlbumCommand() }
}

private suspend fun CommandExecutor.runAlbumCommand() {
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query) \n\n▫ For a better search use the following format: **$label** Artist - Album name") }

    var query = ""
    for (content in args.indices) query += "${args[content]} "

    channel.sendTyping().queue()
    var searchResult = Main.INSTANCE.searchManager.searchAlbums(query, 10)

    var canDispose = false

    var position: Int
    val searchList = StringBuilder()
    if (searchResult.isEmpty()) searchList.append("``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1")
    else {
        position = 1
        for (album in searchResult) searchList.append("``${position++}``. ${album.artists.getFormatted()} - ${album.name} | [link](${album.externalUrls["spotify"]}) \n")
        searchList.append("\n\n If the one you want is not in the list, do a new search by clicking \uD83D\uDCA1")
    }

    val searchMessage = channel.sendMessage(
        EmbedBuilder()
            .setTitle("Album search")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription("<:menu:781976446418812958> Here's the albums list **-** Select the right one by clicking on the emote for his position \n\n$searchList")
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
            .filterNot { searchReactions.containsKey(it.reactionEmote.name) || it.reactionEmote.name == "\uD83D\uDCA1" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }

    searchReactions.keys.take(searchResult.size).forEach { searchMessage.addReaction(it).queue() }
    searchMessage.addReaction("\uD83D\uDCA1").queue()

    searchList.clear()

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == searchMessage.idLong }
                .filter { !it.user.isBot }
                .filter { searchReactions.containsKey(it.reactionEmote.name) || it.reactionEmote.name == "\uD83D\uDCA1" }
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
                searchResult = Main.INSTANCE.searchManager.searchAlbums(query, 10)
                if (searchResult.isEmpty()) searchList.append("``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1")
                else {
                    position = 1
                    for (album in searchResult) searchList.append("``${position++}``. ${album.artists.getFormatted()} - ${album.name} | [link](${album.externalUrls["spotify"]}) \n")
                    searchList.append("\n\n If the one you want is not in the list, do a new search by clicking \uD83D\uDCA1")
                }

                searchMessage.editMessage(
                    EmbedBuilder()
                    .setTitle("Album aearch")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription("<:menu:781976446418812958> Here's the albums list **-** Select the right one by clicking on the emote for his position \n\n$searchList")
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()).await()
                searchReactions.keys.take(searchResult.size).forEach { searchMessage.addReaction(it).queue() }
                searchMessage.addReaction("\uD83D\uDCA1").queue()
            }
            else -> {
                try {
                    val album = Main.INSTANCE.searchManager.getAlbum(searchResult[searchReactions[choice.reactionEmote.name]!!].id)!!

                    var tracks = ""
                    album.tracks.items.forEach { tracks = "$tracks - **${it.name}** (${it.trackNumber}) \n" }

                    searchMessage.editMessage(
                        EmbedBuilder()
                        .setTitle("Album search")
                        .setColor(LIGHT_PINK_COLOR)
                        .setDescription("<:menu:781976446418812958> Here's the album information \n\n__**Album information**__ \n\uD83D\uDCDA **Artist:** ${album.artists.getFormatted()} \n\uD83D\uDCDA **Album:** ${album.name} \n\uD83D\uDCC6 **Release date**: ${if (album.releaseDate != null) "${album.releaseDate.split("-")[2]}/${album.releaseDate.split("-")[1]}/${album.releaseDate.split("-")[0]}" else "Not found"} \n\uD83D\uDCCE **Genres**: ${if (album.genres.isEmpty()) "Not found" else album.genres.getFormatted()} \n\n❤ **Popularity:** ${album.popularity}% \n\n\uD83C\uDFA7 **Tracks:** ${album.tracks.total} \n$tracks \n**©** ${album.copyrights.firstOrNull() ?: "."}")
                        .setThumbnail(album.images.firstOrNull()?.url)
                        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                        .build()).await()
                    searchMessage.clearReactions().queue()
                } catch (ignored: ArrayIndexOutOfBoundsException) {}

                canDispose = true
            }
        }
    }
}