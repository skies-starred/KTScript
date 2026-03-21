# KTScript

KTScript is a barebones `.kts` script loader for Minecraft. The mod is a proof of concept, but should be stable enough to actually use.

Report bugs to @skies.starred on discord.

### How it works

- Scripts are loaded from `./minecraft/config/KTScript/scripts`
- On load, scripts are compiled and cached as jars in `./minecraft/config/KTScript/compiled`
- Cached jars are invalidated automatically when a script is modified, so it always recompiles on change
- Once loaded, scripts behave exactly like a native jar mod, achieving the same performance.

### Events

Scripts should use the KTScript event bus, instead of another event bus like Fabric API's event bus as they will not be cleaned up properly.

### Reload

KTScript supports using the command ``/ktscript reload`` to reload all the scripts in-game.

### Script example

```kotlin
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import xyz.aerii.ktscript.events.core.on
import xyz.aerii.ktscript.events.*

object Test {
    init {
        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            println("Opened screen ${title.string}")
        }
    }
}

on<TickEvent.Server> {
    println("Posted every server tick!")
}
```