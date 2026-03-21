@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    `maven-publish`
}

val mc = stonecutter.current.version
version = "${property("mod.version")}+$mc"
base.archivesName = property("mod.id").toString()

repositories {
    fun strictMaven(url: String, vararg groups: String) = maven(url) { content { groups.forEach(::includeGroupAndSubgroups) } }

    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "me.djtheredstoner")
    strictMaven("https://maven.parchmentmc.org/", "org.parchmentmc")
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("parchment".mc(mc))
    })

    modRuntimeOnly(libs.devauth)

    modImplementation("fabric-api".mc(mc))
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.language.kotlin)

    shadow(libs.bundles.kotlin.extra)
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.named("client") {
        isIdeConfigGenerated = true
        vmArgs.addAll(
            arrayOf(
                "-Ddevauth.enabled=true",
                "-Ddevauth.account=main",
                "-XX:+AllowEnhancedClassRedefinition"
            )
        )
    }

    runConfigs.named("server") {
        isIdeConfigGenerated = false
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

tasks {
    processResources {
        val r = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "kotlin" to libs.versions.fabric.language.kotlin.get()
        )

        inputs.properties(r)
        filesMatching("fabric.mod.json") { expand(r) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

fun String.mc(mc: String): Provider<MinimalExternalModuleDependency> = project.extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("$this-${mc.replace(".", "_")}").get()

fun DependencyHandler.shadow(dep: Any, config: ExternalModuleDependency.() -> Unit = {}) {
    val d = create((dep as? Provider<*>)?.get() ?: dep) as ExternalModuleDependency
    d.config()
    include(d)
    modImplementation(d)
}

fun DependencyHandler.shadow(bundle: Provider<ExternalModuleDependencyBundle>, config: ExternalModuleDependency.() -> Unit = {}) {
    bundle.get().forEach {
        val d = create(it) as ExternalModuleDependency
        d.config()
        include(d)
        modImplementation(d)
    }
}