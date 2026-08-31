package foo.starred.ktscript.api.lifecycle.managed.extensions

import foo.starred.ktscript.api.lifecycle.managed.impl.KTScriptLifeCycle

fun unload(block: () -> Unit) {
    KTScriptLifeCycle.unloads.add(block)
}

fun <T : Any> T.autoClose(close: T.() -> Unit): T {
    unload {
        close(this)
    }

    return this
}

fun <T : AutoCloseable> T.autoClose(): T {
    unload {
        close()
    }

    return this
}
