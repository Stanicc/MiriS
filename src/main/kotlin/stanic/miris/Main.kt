package stanic.miris

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity

class Main {

    lateinit var jda: JDA

    companion object {
        lateinit var INSTANCE: Main

        @JvmStatic fun main(args: Array<String>) {
            INSTANCE = Main()

            JDABuilder.createDefault(args[0])
                .setActivity(Activity.playing("MiriS!"))
                .build()
                .awaitReady().runCatching {
                    INSTANCE.jda = this
                }
        }
    }

}