package foo.starred.ktscript.api.lifecycle.managed.impl

import foo.starred.snowbird.utils.safely
import java.util.concurrent.CopyOnWriteArrayList

object KTScriptLifeCycle {
    val unloads = CopyOnWriteArrayList<() -> Unit>()

    fun cleanup() {
        for (hook in unloads) {
            safely {
                hook()
            }
        }

        unloads.clear()
    }
}
