@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
}

val ver = stonecutter.current.version
val modId = project.property("mod.id").toString()
val modName = project.property("mod.name").toString()
val modVer = project.property("mod.version").toString()

version = "$modVer+$ver"
base.archivesName = modId

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.starred.foo/releases")
    maven("https://maven.starred.foo/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:$ver")

    localRuntime("devauth".global)

    implementation("fabric-api".versioned)
    implementation("fabric-loader".global)
    implementation("fabric-language-kotlin".global)

    shadow("kommand".global)
    shadow("snowbird".versioned)
    for (dep in "kotlin-extra".bundle.get()) shadow(dep)
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/$modId.accesswidener")

    runConfigs.named("client") {
        generateRunConfig = true
        jvmArguments.addAll(
            "-Ddevauth.enabled=true",
            "-Ddevauth.account=main",
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+IgnoreUnrecognizedVMOptions",
        )
    }

    runConfigs.named("server") {
        generateRunConfig = false
    }
}


tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)

    compilerOptions {
        jvmTarget.set(JvmTarget.valueOf("JVM_25"))
    }
}

tasks {
    processResources {
        val r = mapOf("id" to modId, "name" to modName, "version" to modVer, "minecraft" to project.property("mod.mc_dep"), "accessWidener" to "$modId.accesswidener")

        inputs.properties(r)
        filesMatching("fabric.mod.json") { expand(r) }
    }

    register<Copy>("buildAndCollect") {
        description = "Builds and collects mod jars."
        group = "build"
        from(jar, kotlinSourcesJar)
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

val String.global: Provider<MinimalExternalModuleDependency>
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary(this).get()

val String.bundle: Provider<ExternalModuleDependencyBundle>
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs").findBundle(this).get()

val String.versioned: Provider<MinimalExternalModuleDependency>
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("$this-${ver.replace(".", "_")}").get()

fun DependencyHandlerScope.shadow(dep: Any, config: ExternalModuleDependency.() -> Unit = {}) {
    val d = create((dep as? Provider<*>)?.get() ?: dep) as ExternalModuleDependency
    d.config()
    include(d)
    implementation(d)
}
