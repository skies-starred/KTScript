@file:Suppress("Unused")

package xyz.aerii.ktscript.events

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import xyz.aerii.ktscript.events.core.CancellableEvent
import xyz.aerii.ktscript.events.core.Event
import xyz.aerii.ktscript.handlers.Typo.stripped

sealed class GuiEvent {
    sealed class Render {
        data class Pre(
            val graphics: GuiGraphics
        ) : Event()

        data class Post(
            val graphics: GuiGraphics
        ) : Event()
    }

    sealed class Container {
        sealed class Render {
            data class Pre(
                val graphics: GuiGraphics
            ) : CancellableEvent()
        }
    }

    sealed class Open {
        data class Container(
            val screen: AbstractContainerScreen<*>
        ) : Event() {
            val stripped = screen.title.stripped()
        }

        data class Any(
            val screen: Screen
        ) : Event() {
            val stripped = screen.title.stripped()
        }
    }

    sealed class Close {
        data class Container(
            val screen: AbstractContainerScreen<*>
        ) : Event() {
            val stripped = screen.title.stripped()
        }

        data class Any(
            val screen: Screen
        ) : Event() {
            val stripped = screen.title.stripped()
        }
    }

    sealed class Slots {
        sealed class Render {
            data class Pre(
                val graphics: GuiGraphics,
                val slot: Slot
            ) : CancellableEvent()

            data class Post(
                val graphics: GuiGraphics,
                val slot: Slot
            ) : Event()
        }

        data class Click(
            val slot: Slot?,
            val slotId: Int,
            val mouseButton: Int,
            val clickType: ClickType
        ) : CancellableEvent()

        data class Hover(
            val slot: Slot
        ) : Event()
    }

    sealed class Items {
        sealed class Render {
            data class Pre(
                val graphics: GuiGraphics,
                val item: ItemStack,
                val x: Int,
                val y: Int
            ) : Event()

            data class Post(
                val graphics: GuiGraphics,
                val item: ItemStack,
                val x: Int,
                val y: Int
            ) : Event()
        }
    }

    sealed class Tooltip {
        data class Render(
            val item: ItemStack,
            val tooltip: MutableList<Component>
        ) : Event()
    }

    sealed class Input {
        sealed class Key {
            data class Press(
                val keyEvent: KeyEvent
            ) : CancellableEvent()

            data class Release(
                val keyEvent: KeyEvent
            ) : Event()
        }

        sealed class Mouse {
            data class Press(
                val keyEvent: MouseButtonEvent
            ) : CancellableEvent()

            data class Release(
                val keyEvent: MouseButtonEvent
            ) : Event()

            data class Scroll(
                val amount: Double
            ) : CancellableEvent()
        }
    }
}
