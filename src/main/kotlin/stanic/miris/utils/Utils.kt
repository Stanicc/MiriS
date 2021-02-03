package stanic.miris.utils

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import kotlin.coroutines.*
import kotlinx.coroutines.*

fun TextChannel.reply(message: String) = sendMessage(message).queue()
fun TextChannel.reply(message: String, success: Message.() -> Unit) = sendMessage(message).queue {
    it.apply(success)
}
fun TextChannel.replyDeleting(message: String, time: Long = 10000) = sendMessage(message).queue {
    GlobalScope.launch {
        delay(time)
        it.delete().queue()
        cancel("terminated")
    }
}
fun TextChannel.reply(embed: MessageEmbed) = sendMessage(embed).queue()
fun TextChannel.replyDeleting(embed: MessageEmbed, time: Long = 10000) = sendMessage(embed).queue {
    GlobalScope.launch {
        delay(time)
        it.delete().queue()
        cancel("terminated")
    }
}

fun Member.reply(message: String) = user.openPrivateChannel().complete().sendMessage(message).complete()
fun Member.reply(embed: MessageEmbed) = user.openPrivateChannel().complete().sendMessage(embed).complete()

fun List<String>.toMessage(): String {
    var message = ""
    for (line in this) message = "$message$line\n"

    return message
}

suspend fun <T> RestAction<T>.await() = suspendCoroutine<T> { continuation ->
    queue(
        continuation::resume,
        continuation::resumeWithException
    )
}