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
import stanic.miris.utils.LIGHT_PINK_COLOR
import stanic.miris.utils.await
import stanic.miris.utils.bot.*
import stanic.miris.utils.replyDeleting

fun CommandListDefinition.registerLyricsCommand() {
    command("lyrics") { runLyricsCommand() }
    command("lyric") { runLyricsCommand() }
    command("letras") { runLyricsCommand() }
    command("letra") { runLyricsCommand() }
}

private suspend fun CommandExecutor.runLyricsCommand() {
    if (args.isEmpty()) fail { channel.replyDeleting(":x: | Use **$label** (query)") }

    var query = ""
    for (content in args.indices) query += "${args[content]} "

    channel.sendTyping().queue()
    var lyrics = Main.INSTANCE.searchManager.searchLyrics(query)

    var description =
        if (lyrics == null) "``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1" else "<:menu:781976446418812958> Here's the found artist **-** If that is what you were looking for click on ${YES_PINK.asMention} to see the information \n\nName: ${lyrics.title} \nArtist: ${lyrics.author} \n\nIf you want to do a new search click on \uD83D\uDCA1"
    val searchMessage = channel.sendMessage(
        EmbedBuilder()
            .setTitle("Lyrics search")
            .setColor(LIGHT_PINK_COLOR)
            .setDescription(description)
            .setThumbnail(lyrics?.image)
            .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
            .build()
    ).await()

    onDispose {
        message.delete().queue()
        try {
            delay(120000)
            searchMessage.delete().queue()
        } catch (ignored: Exception) {
        }
    }

    setup {
        on<GuildMessageReceivedEvent>().asFlow()
            .filter { it.channel == channel }
            .launchIn(GlobalScope)
        on<GuildMessageReactionAddEvent>().asFlow()
            .filter { it.messageIdLong == searchMessage.idLong }
            .filterNot { it.reactionEmote.name == YES_PINK.name || it.reactionEmote.name == "\uD83D\uDCA1" || it.reactionEmote.name == PINK_ARROW_RIGHT.name || it.reactionEmote.name == PINK_ARROW_LEFT.name }
            .onEach { it.reaction.removeReaction(it.user).submit().await() }
            .launchIn(GlobalScope)
    }
    if (lyrics != null) searchMessage.addReaction(YES_PINK).queue()
    searchMessage.addReaction("\uD83D\uDCA1").queue()

    var canDispose = false

    while (!canDispose) {
        var choice = withTimeoutOrNull(20000) {
            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                .filter { it.messageIdLong == searchMessage.idLong }
                .filter { !it.user.isBot }
                .filter { it.reactionEmote.name == YES_PINK.name || it.reactionEmote.name == "\uD83D\uDCA1" }
                .awaitFirst()
        } ?: fail {
            canDispose = true
            try {
                searchMessage.delete().queue()
            } catch (ignored: Exception) {
            }
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
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }

                query = newQuery.message.contentRaw

                newQuery.message.delete().queue()
                lyrics = Main.INSTANCE.searchManager.searchLyrics(query)

                description =
                    if (lyrics == null) "``-`` I couldn't find anything! If you want to do a new search click on \uD83D\uDCA1" else "<:menu:781976446418812958> Here's the found artist **-** If that is what you were looking for click on ${YES_PINK.asMention} to see the information \n\nName: ${lyrics.title} \nArtist: ${lyrics.author} \n\nIf you want to do a new search click on \uD83D\uDCA1"
                searchMessage.editMessage(
                    EmbedBuilder()
                        .setTitle("Lyrics search")
                        .setColor(LIGHT_PINK_COLOR)
                        .setDescription(description)
                        .setThumbnail(lyrics?.image)
                        .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                        .build()
                ).await()
                if (lyrics != null) searchMessage.addReaction(YES_PINK).queue()
                searchMessage.addReaction("\uD83D\uDCA1").queue()
            }
            else -> {
                val pages = ArrayList<String>(lyrics!!.content.chunked(1000))
                var currentPage = 0

                if (pages.size == 1) {
                    searchMessage.editMessage(
                        EmbedBuilder()
                            .setTitle("Lyrics search")
                            .setColor(LIGHT_PINK_COLOR)
                            .setDescription("<:menu:781976446418812958> Here's the lyrics information \n\n__**Lyrics information**__ \n\uD83C\uDFA7 **Title:** ${lyrics.title} \n\uD83D\uDCC4 **Artist:** ${lyrics.author} \n\n${lyrics.content}")
                            .setThumbnail(lyrics.image)
                            .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                            .build()
                    ).await()
                    searchMessage.clearReactions().queue()
                    canDispose = true
                } else {
                    var content = pages[currentPage]

                    searchMessage.editMessage(
                        EmbedBuilder()
                            .setTitle("Lyrics search")
                            .setColor(LIGHT_PINK_COLOR)
                            .setDescription("<:menu:781976446418812958> Here's the lyrics information \n\n__**Lyrics information**__ \n\uD83C\uDFA7 **Title:** ${lyrics.title} \n\uD83D\uDCD6 **Page:** ${currentPage + 1}/${pages.size} \n\uD83D\uDCC4 **Artist:** ${lyrics.author} \n\n$content${if (pages.size > (currentPage + 1)) "**-** \n\n*Change the page to continue the lyrics*" else ""}")
                            .setThumbnail(lyrics.image)
                            .setFooter("Requested by ${member.nickname ?: member.user.name}", member.user.avatarUrl)
                            .build()
                    ).await()
                    searchMessage.clearReactions().queue()

                    searchMessage.addReaction(PINK_ARROW_LEFT).queue()
                    searchMessage.addReaction(PINK_ARROW_RIGHT).queue()

                    while (!canDispose) {
                        choice = withTimeoutOrNull(20000) {
                            Main.INSTANCE.manager.on<GuildMessageReactionAddEvent>()
                                .filter { it.messageIdLong == searchMessage.idLong }
                                .filter { !it.user.isBot }
                                .filter { it.reactionEmote.name == PINK_ARROW_RIGHT.name || it.reactionEmote.name == PINK_ARROW_LEFT.name }
                                .awaitFirst()
                        } ?: fail { canDispose = true }

                        when (choice.reactionEmote.name) {
                            PINK_ARROW_RIGHT.name -> {
                                if (currentPage < (pages.size - 1)) {
                                    currentPage += 1
                                    content = pages[currentPage]

                                    searchMessage.editMessage(
                                        EmbedBuilder()
                                            .setTitle("Lyrics search")
                                            .setColor(LIGHT_PINK_COLOR)
                                            .setDescription("<:menu:781976446418812958> Here's the lyrics information \n\n__**Lyrics information**__ \n\uD83C\uDFA7 **Title:** ${lyrics.title} \n\uD83D\uDCD6 **Page:** ${currentPage + 1}/${pages.size} \n\uD83D\uDCC4 **Artist:** ${lyrics.author} \n\n$content${if (pages.size > (currentPage + 1)) "**-** \n\n*Change the page to continue the lyrics*" else ""}")
                                            .setThumbnail(lyrics.image)
                                            .setFooter(
                                                "Requested by ${member.nickname ?: member.user.name}",
                                                member.user.avatarUrl
                                            )
                                            .build()
                                    ).await()
                                }
                            }
                            PINK_ARROW_LEFT.name -> {
                                if (currentPage != 0) {
                                    currentPage -= 1
                                    content = pages[currentPage]

                                    searchMessage.editMessage(
                                        EmbedBuilder()
                                            .setTitle("Lyrics search")
                                            .setColor(LIGHT_PINK_COLOR)
                                            .setDescription("<:menu:781976446418812958> Here's the lyrics information \n\n__**Lyrics information**__ \n\uD83C\uDFA7 **Title:** ${lyrics.title} \n\uD83D\uDCD6 **Page:** ${currentPage + 1}/${pages.size} \n\uD83D\uDCC4 **Artist:** ${lyrics.author} \n\n$content${if (pages.size > (currentPage + 1)) "**-** \n\n*Change the page page to continue the lyrics*" else ""}")
                                            .setThumbnail(lyrics.image)
                                            .setFooter(
                                                "Requested by ${member.nickname ?: member.user.name}",
                                                member.user.avatarUrl
                                            )
                                            .build()
                                    ).await()
                                }
                            }
                        }

                        choice.reaction.removeReaction(member.user).queue()
                    }
                }
            }
        }
    }
}
