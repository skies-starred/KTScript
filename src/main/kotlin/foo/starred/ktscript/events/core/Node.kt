package foo.starred.ktscript.events.core

import foo.starred.ktscript.KTScript
import foo.starred.snowbird.api.data.Observable
import java.util.concurrent.atomic.AtomicBoolean

class Node<T : Event>(
    @JvmField val eventClass: Class<T>,
    @JvmField var handler: (T) -> Unit,
    @JvmField val priority: Int
) {
    private val state = AtomicBoolean(false)
    internal var overridden = false
    internal val conditions = mutableListOf<Observable<Boolean>>()

    init {
        KTScript.nodes.add(this)
    }

    fun once() = apply {
        val original = handler
        handler = {
            original(it)
            unregister()
            KTScript.nodes.remove(this)
        }
    }

    fun register(): Boolean {
        if (!state.compareAndSet(false, true)) return false
        EventBus.add(this)
        return true
    }

    fun unregister(): Boolean {
        if (!state.compareAndSet(true, false)) return false
        EventBus.remove(this)
        return true
    }

    internal fun evaluate() = if (conditions.all { it.value }) register() else unregister()
}
