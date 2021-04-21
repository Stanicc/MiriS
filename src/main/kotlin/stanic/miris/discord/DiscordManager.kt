package stanic.miris.discord

import br.com.devsrsouza.jda.command.commands
import net.dv8tion.jda.api.JDA
import stanic.miris.discord.commands.information.*
import stanic.miris.discord.commands.misc.*
import stanic.miris.discord.commands.music.*
import stanic.miris.discord.commands.effect.*
import stanic.miris.discord.commands.search.*

fun JDA.registerCommands() = commands("ms") {
    //music
    registerPlayCommand()
    registerStopCommand()
    registerResumeCommand()
    registerPauseCommand()
    registerSkipCommand()
    registerRestartCommand()
    registerLoopCommand()
    registerVolumeCommand()

    //effect
    registerBassCommand()

    //misc
    registerJoinCommand()

    //information
    registerInfoCommand()
    registerQueueCommand()

    //search
    registerSearchCommand()
    registerArtistCommand()
    registerAlbumCommand()
    registerLyricsCommand()
}

fun JDA.registerListeners() {

}