package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.Event

sealed class TickEvent {
    sealed class Client {
        data object Start : Event()

        data object End : Event()
    }

    data object Server : Event()
}
