package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.CancellableEvent
import foo.starred.ktscript.events.core.Event
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonInfo

sealed class InputEvent {
    sealed class Keyboard {
        data class Press(
            val keyEvent: KeyEvent
        ) : CancellableEvent()

        data class Release(
            val keyEvent: KeyEvent
        ) : Event()
    }

    sealed class Mouse {
        data class Press(
            val buttonInfo: MouseButtonInfo
        ) : CancellableEvent()

        data class Release(
            val buttonInfo: MouseButtonInfo
        ) : Event()

        data class Move(
            val x: Double,
            val y: Double
        ) : CancellableEvent()
    }
}
