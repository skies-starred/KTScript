package xyz.aerii.ktscript.events

import net.minecraft.network.protocol.Packet
import xyz.aerii.ktscript.events.core.CancellableEvent

sealed class PacketEvent(open val packet: Packet<*>) : CancellableEvent() {
    sealed class Process(override val packet: Packet<*>) : PacketEvent(packet) {
        data class Pre(
            override val packet: Packet<*>
        ) : Process(packet)

        data class Post(
            override val packet: Packet<*>
        ) : Process(packet)
    }

    data class Receive(
        override val packet: Packet<*>
    ) : PacketEvent(packet)

    data class Send(
        override val packet: Packet<*>
    ) : PacketEvent(packet)
}