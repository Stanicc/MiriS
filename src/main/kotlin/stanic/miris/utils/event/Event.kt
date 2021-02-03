package stanic.miris.utils.event

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

inline fun <reified T : Event> JDA.event(crossinline block: T.() -> Unit) {
    addEventListener(object : ListenerAdapter() {
        override fun onGenericEvent(event: GenericEvent) {
            (event as? T)?.block()
        }
    })
}