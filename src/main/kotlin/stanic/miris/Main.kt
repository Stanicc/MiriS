package stanic.miris

import club.minnced.jda.reactor.ReactiveEventManager
import net.dv8tion.jda.api.*
import net.dv8tion.jda.api.entities.Activity
import stanic.miris.discord.*
import stanic.miris.manager.*

class Main {

    lateinit var jda: JDA
    lateinit var manager: ReactiveEventManager
    lateinit var searchManager: SearchManager

    companion object {
        lateinit var INSTANCE: Main

        @JvmStatic fun main(args: Array<String>) {
            INSTANCE = Main()
            INSTANCE.manager = ReactiveEventManager()
            INSTANCE.searchManager = SearchManager().apply { enable() }

            JDABuilder.createDefault(args[0])
                .setActivity(Activity.playing("MiriS!"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setEventManager(INSTANCE.manager)
                .setAutoReconnect(true)
                .build()
                .awaitReady().runCatching {
                    INSTANCE.jda = this
                    MusicManager().start()

                    registerCommands()
                    registerListeners()
                }
        }
    }

}