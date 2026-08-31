package foo.starred.ktscript.api.lifecycle.singletons.impl

import java.util.concurrent.ConcurrentHashMap

object KTScriptSingletons {
    val store = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrCreate(key: String, create: () -> T): T {
        return store.computeIfAbsent(key) {
            create()
        } as T
    }

    fun remove(set: Set<String>) {
        store.keys.removeIf { a ->
            set.none { b ->
                a.startsWith("$b:")
            }
        }
    }
}
