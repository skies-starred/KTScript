@file:Suppress("UNCHECKED_CAST")

package xyz.aerii.ktscript.events.core

import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val all = ConcurrentHashMap<Class<out Event>, Array<Node<out Event>>>()

    fun <T : Event> post(event: T) {
        val nodes = all[event.javaClass] as? Array<Node<T>> ?: return
        for (i in nodes) i.handler(event)
    }

    fun add(node: Node<*>) {
        val cls = node.eventClass

        while (true) {
            val old = all[cls]
            val size = old?.size ?: 0

            if (old?.any { it === node } == true) return
            val new = arrayOfNulls<Node<*>>(size + 1) as Array<Node<*>>
            var i = 0

            old?.let {
                while (i < size && old[i].priority <= node.priority) new[i] = old[i].also { i++ }
                if (i < size) System.arraycopy(old, i, new, i + 1, size - i)
            }

            new[i] = node
            if (old == null && all.putIfAbsent(cls, new) == null || old != null && all.replace(cls, old, new)) return
        }
    }

    fun remove(node: Node<*>) {
        val cls = node.eventClass

        while (true) {
            val old = all[cls] ?: return
            val size = old.size
            val index = old.indexOfFirst { it === node }
            if (index == -1) return

            if (size == 1) {
                if (all.remove(cls, old)) return
                continue
            }

            val new = arrayOfNulls<Node<*>>(size - 1) as Array<Node<*>>
            if (index > 0) System.arraycopy(old, 0, new, 0, index)
            if (index < size - 1) System.arraycopy(old, index + 1, new, index, size - index - 1)

            if (all.replace(cls, old, new)) return
        }
    }
}