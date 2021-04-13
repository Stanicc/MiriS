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

fun CommandListDefinition.registerArtistCommand() {
    command("artist") { runArtistCommand() }
    command("artista") { runArtistCommand() }
}

private suspend fun CommandExecutor.runArtistCommand() {
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query)") }

    var query = ""
    for (content in args.indices) query += "${args[content]} "

    channel.sendTyping().queue()
    var artist = Main.INSTANCE.searchManager.searchArtist(query)

    var description = if (artist == null) "``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1" else "<:menu:781976446418812958> Here's the found artist **-** If that is what you were looking for click on ✅ to see the information \n\nName: ${artist.name} \n[spotify link](${artist.externalUrls.externalUrls["spotify"]}) \n\nIf you want to do a new search click on \uD83D\uDCA1"
    val searchMessage = channel.sendMessage(
        EmbedBuilder()
            .setTitle("Artist search")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription(description)
            .setThumbnail(artist?.images?.firstOrNull()?.url)
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
            .filterNot { it.reactionEmote.name == "✅" || it.reactionEmote.name == "\uD83D\uDCA1" }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }
    if (artist != null) searchMessage.addReaction("✅").queue()
    searchMessage.addReaction("\uD83D\uDCA1").queue()

    var canDispose = false

    while (!canDispose) {
        val choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == searchMessage.idLong }
                .filter { !it.user.isBot }
                .filter { it.reactionEmote.name == "✅" || it.reactionEmote.name == "\uD83D\uDCA1" }
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
                artist = Main.INSTANCE.searchManager.searchArtist(query)
                description = if (artist == null) "``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1" else "<:menu:781976446418812958> Here's the found artist **-** If that is what you were looking for click on ✅ to see the information \n\nName: ${artist.name} \n[spotify link](${artist.href}) \n\nIf you want to do a new search click on \uD83D\uDCA1"

                searchMessage.editMessage(EmbedBuilder()
                    .setTitle("Artist search")
                    .setColor(LIGHT_PINK_COLOR)
                    .setDescription(description)
                    .setThumbnail(artist?.images?.firstOrNull()?.url)
                    .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                    .build()).await()
                if (artist != null) searchMessage.addReaction("✅").queue()
                searchMessage.addReaction("\uD83D\uDCA1").queue()
            }
            else -> {
                try {
                    searchMessage.editMessage(EmbedBuilder()
                        .setTitle("Artist search")
                        .setColor(LIGHT_PINK_COLOR)
                        .setDescription("<:menu:781976446418812958> Here's the artist information \n\n__**Artist information**__ \n\uD83D\uDCC4 **Artist:** ${artist!!.name} \n\uD83D\uDC8D **Followers:** ${artist.followers.total} \n\uD83D\uDCCE **Genres:** ${if (artist.genres.isEmpty()) "Not found" else artist.genres.getFormatted()} \n\n❤ **Popularity:** ${artist.popularity}% \n\n\uD83D\uDCCC [spotify link](${artist.externalUrls.externalUrls["spotify"]})")
                        .setThumbnail(artist.images.first().url)
                        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                        .build()).await()
                    searchMessage.clearReactions().queue()
                } catch (ignored: ArrayIndexOutOfBoundsException) {}

                canDispose = true
            }
        }
    }
}