package foo.starred.ktscript.api.lifecycle.singletons.extensions

import foo.starred.ktscript.KTScriptHost
import foo.starred.ktscript.api.lifecycle.singletons.impl.KTScriptSingletons
import foo.starred.ktscript.modifiers.PackageModifier

inline fun <reified T : Any> persist(key: String = T::class.java.name, noinline create: () -> T): T {
    return KTScriptSingletons.getOrCreate(qualify(key), create)
}

inline fun <reified T : Any> singleton(key: String = T::class.java.name, noinline create: () -> T): T {
    return KTScriptSingletons.getOrCreate(qualify(key), create)
}

fun once(key: String, block: () -> Unit) {
    KTScriptSingletons.store.computeIfAbsent(qualify(key)) {
        block()
        true
    }
}

fun qualify(key: String): String {
    val name = KTScriptHost.current.get()?.let { PackageModifier.module(it) } ?: return key
    return if (key.startsWith("$name:")) key else "$name:$key"
}
