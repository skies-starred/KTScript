package xyz.aerii.ktscript.events

import net.minecraft.network.protocol.Packet
import xyz.aerii.ktscript.events.core.CancellableEvent

sealed class PacketEvent(open val packet: Packet<*>) : CancellableEvent() {
    data class Receive(
        override val packet: Packet<*>
    ) : PacketEvent(packet)

    data class Send(
        override val packet: Packet<*>
    ) : PacketEvent(packet)
}