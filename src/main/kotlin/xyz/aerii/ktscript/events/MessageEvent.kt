@file:Suppress("Unused")

package xyz.aerii.ktscript.events

import net.minecraft.network.chat.Component
import xyz.aerii.ktscript.events.core.CancellableEvent
import xyz.aerii.ktscript.events.core.Event
import xyz.aerii.ktscript.handlers.Typo.stripped

sealed class MessageEvent {
    sealed class Chat {
        data class Intercept(val message: Component) : CancellableEvent() {
            val stripped = message.stripped()
        }

        data class Receive(val message: Component) : Event() {
            val stripped = message.stripped()
        }
    }

    sealed class Title {
        data class Main(val message: Component) : CancellableEvent()

        data class Sub(val message: Component) : CancellableEvent()
    }

    data class ActionBar(val message: Component) : Event()
}