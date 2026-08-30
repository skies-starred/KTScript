@file:Suppress("Unused")

package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.CancellableEvent
import foo.starred.ktscript.events.core.Event
import foo.starred.snowbird.utils.stripped
import net.minecraft.network.chat.Component

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
