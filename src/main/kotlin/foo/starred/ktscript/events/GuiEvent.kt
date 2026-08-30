@file:Suppress("Unused")

package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.CancellableEvent
import foo.starred.ktscript.events.core.Event
import foo.starred.snowbird.utils.stripped
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

sealed class GuiEvent {
    sealed class Render {
        data class Pre(
            val graphics: GuiGraphicsExtractor
        ) : Event()

        data class Main(
            val graphics: GuiGraphicsExtractor
        ) : Event()

        data class Post(
            val graphics: GuiGraphicsExtractor
        ) : Event()

        sealed class Container {
            data class Pre(
                val graphics: GuiGraphicsExtractor
            ) : CancellableEvent()
        }

        sealed class Screen {
            data class Pre(
                val graphics: GuiGraphicsExtractor
            ) : CancellableEvent()

            data class Post(
                val graphics: GuiGraphicsExtractor
            ) : Event()
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
                val graphics: GuiGraphicsExtractor,
                val slot: Slot
            ) : CancellableEvent()

            data class Post(
                val graphics: GuiGraphicsExtractor,
                val slot: Slot
            ) : Event()

            sealed class Hotbar {
                data class Pre(
                    val graphics: GuiGraphicsExtractor,
                    val item: ItemStack,
                    val x: Int,
                    val y: Int
                ) : CancellableEvent()

                data class Post(
                    val graphics: GuiGraphicsExtractor,
                    val item: ItemStack,
                    val x: Int,
                    val y: Int
                ) : Event()
            }
        }

        data class Click(
            val slot: Slot?,
            val slotId: Int,
            val mouseButton: Int,
            val clickType: ContainerInput
        ) : CancellableEvent()

        data class Hover(
            val slot: Slot
        ) : Event()
    }

    sealed class Items {
        sealed class Render {
            data class Pre(
                val graphics: GuiGraphicsExtractor,
                val item: ItemStack,
                val x: Int,
                val y: Int
            ) : Event()

            data class Post(
                val graphics: GuiGraphicsExtractor,
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
