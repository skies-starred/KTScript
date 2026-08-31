package foo.starred.ktscript.api.lifecycle.singletons.extensions

import foo.starred.ktscript.KTScriptHost
import foo.starred.ktscript.api.lifecycle.singletons.impl.KTScriptSingletons
import foo.starred.ktscript.modifiers.PackageModifier
import kotlin.reflect.KProperty

class PersistedProperty<T : Any>(
    private val key: String,
    default: () -> T
) {
    @Suppress("UNCHECKED_CAST")
    var value: T = KTScriptSingletons.getOrCreate(key, default)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this@PersistedProperty.value = value
        KTScriptSingletons.store[key] = value
    }
}

class PersistedPropertyProvider<T : Any>(
    private val key: String,
    private val default: () -> T
) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): PersistedProperty<T> {
        val script = KTScriptHost.current.get()?.let { PackageModifier.module(it) } ?: thisRef?.javaClass?.name ?: "global"
        val key = key.ifEmpty { property.name }.let { if (it.startsWith("$script:")) it else "$script:$it" }
        return PersistedProperty(key, default)
    }
}

fun <T : Any> state(default: T): PersistedPropertyProvider<T> {
    return PersistedPropertyProvider("") { default }
}

fun <T : Any> state(key: String = "", default: () -> T): PersistedPropertyProvider<T> {
    return PersistedPropertyProvider(key, default)
}
