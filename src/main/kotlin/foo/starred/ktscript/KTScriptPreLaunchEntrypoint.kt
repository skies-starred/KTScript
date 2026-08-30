package foo.starred.ktscript

import foo.starred.ktscript.KTScript.LOGGER
import foo.starred.ktscript.api.network.WebAPI
import foo.starred.snowbird.api.network.builders.impl.DownloadBuilder
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import java.nio.file.Files
import java.nio.file.Path

object KTScriptPreLaunchEntrypoint : PreLaunchEntrypoint {
    private val list = listOf("kotlin-compiler-embeddable", "kotlin-scripting-compiler-embeddable", "kotlin-scripting-compiler-impl-embeddable", "kotlin-daemon-embeddable")
    private val directory: Path = FabricLoader.getInstance().configDir.resolve("KTScript/bundled")

    override fun onPreLaunch() {
        LOGGER.info("Loading Kotlin Scripting compiler dependencies...")
        Files.createDirectories(directory)

        for (id in list) {
            FabricLauncherBase.getLauncher().addToClassPath(get(id))
        }

        LOGGER.info("Kotlin compiler dependencies added to classpath.")
    }

    private fun get(id: String): Path {
        val name = "$id-2.4.10.jar"
        val path = directory.resolve(name)
        if (Files.isRegularFile(path) && Files.size(path) > 0L) return path

        runBlocking {
            DownloadBuilder("https://repo1.maven.org/maven2/org/jetbrains/kotlin/$id/2.4.10/$name", path.toFile(), true, WebAPI).run()
        }

        return path
    }
}
