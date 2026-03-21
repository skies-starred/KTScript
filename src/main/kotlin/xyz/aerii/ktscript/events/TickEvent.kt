package xyz.aerii.ktscript.events

import xyz.aerii.ktscript.events.core.Event

sealed class TickEvent {
    sealed class Client {
        data object Start : Event()

        data object End : Event()
    }

    data object Server : Event()
}