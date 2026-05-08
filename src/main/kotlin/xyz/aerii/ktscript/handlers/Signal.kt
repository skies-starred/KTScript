package xyz.aerii.ktscript.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import xyz.aerii.ktscript.events.EntityEvent
import xyz.aerii.ktscript.events.GuiEvent
import xyz.aerii.ktscript.events.MessageEvent
import xyz.aerii.ktscript.events.PacketEvent
import xyz.aerii.ktscript.events.TickEvent
import xyz.aerii.ktscript.events.WorldRenderEvent
import xyz.aerii.ktscript.events.core.on
import xyz.aerii.ktscript.utils.mainThread

object Signal {
    fun init() {
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenMouseEvents.allowMouseClick(screen).register { _, event ->
                !GuiEvent.Input.Mouse.Press(event).post()
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, event ->
                !GuiEvent.Input.Mouse.Release(event).post()
            }

            ScreenMouseEvents.allowMouseScroll(screen).register { _, _, _, _, amount ->
                !GuiEvent.Input.Mouse.Scroll(amount).post()
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, event ->
                !GuiEvent.Input.Key.Press(event).post()
            }

            ScreenKeyboardEvents.allowKeyRelease(screen).register { _, event ->
                !GuiEvent.Input.Key.Release(event).post()
            }
        }

        WorldRenderEvents.END_MAIN.register { context ->
            WorldRenderEvent.Last(context).post()
        }

        ItemTooltipCallback.EVENT.register { stack, _, _, components ->
            GuiEvent.Tooltip.Render(stack, components).post()
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            EntityEvent.Load(entity).post()
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            EntityEvent.Unload(entity).post()
        }

        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            TickEvent.Client.Start.post()
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            TickEvent.Client.End.post()
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { component, bool ->
            if (bool) return@register true
            !MessageEvent.Chat.Intercept(component).post()
        }

        on<PacketEvent.Receive, ClientboundSystemChatPacket> {
            mainThread {
                (if (this@on.overlay) MessageEvent.ActionBar(content) else MessageEvent.Chat.Receive(content)).post()
            }
        }

        on<PacketEvent.Receive, ClientboundSetTitleTextPacket> {
            if (MessageEvent.Title.Main(text).post()) it.cancel()
        }

        on<PacketEvent.Receive, ClientboundSetSubtitleTextPacket> {
            if (MessageEvent.Title.Sub(text).post()) it.cancel()
        }
    }
}