# KTScript

KTScript is a `.kts` script loader for Minecraft.

### How it works

- Scripts are loaded from `./minecraft/config/KTScript/scripts`
- On load, scripts are compiled and cached as jars in `./minecraft/config/KTScript/compiled`
- Cached jars are invalidated automatically when a script is modified, so it always recompiles on change
- Once loaded, scripts behave exactly like a native jar mod, achieving the same performance.

### Events

Scripts should use the KTScript event bus, instead of another event bus like Fabric API's event bus as they will not be cleaned up properly.

If you want a specific event to be added to the custom event bus, please let me know!

### Reload

KTScript supports using the command ``/ktscript reload`` to reload all the scripts in-game.

### Utility files

The mod includes my [library](https://github.com/skies-starred/library) that you can use in the code. There is no documentation currently so you'll have to look through the library's code for anything that you need.

### Script example

```kotlin
@file:Mod(
    name = "ExampleMod",
    description = "Example mod",
    version = "1.0.0",
    author = "Starred"
)

@file:Dependencies(
    mods = [], // array of mod ids
    minecraft = ["26.1.*"] // array of minecraft versions
)

// automatically set to ktscript.scripts.folderName if not set
package com.example.ktscript

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket

object Test {
    init {
        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            println("Opened screen ${title.string}")
        }
    }
}

once("init") {
    println("Initialised once!")
}

val sessionStart = persist("sessionStart") { System.currentTimeMillis() }

// Mixins!
hook<Minecraft>("setScreen") { (screen: Screen?) ->
    println("Screen changed to: $screen")
}

on<TickEvent.Client.Start> {
    // Runs every client tick
}
```
