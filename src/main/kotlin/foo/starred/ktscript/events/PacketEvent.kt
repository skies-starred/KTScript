package foo.starred.ktscript.events

import foo.starred.ktscript.events.core.CancellableEvent
import net.minecraft.network.protocol.Packet

sealed class PacketEvent(open val packet: Packet<*>) : CancellableEvent() {
    data class Receive(
        override val packet: Packet<*>
    ) : PacketEvent(packet)

    data class Send(
        override val packet: Packet<*>
    ) : PacketEvent(packet)
}
