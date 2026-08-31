package foo.starred.ktscript.api.hooks.impl

import foo.starred.ktscript.api.hooks.context.HookContext
import foo.starred.ktscript.api.hooks.data.HookAt

object HookDispatcher {
    @JvmStatic
    fun enter(method: String, instance: Any?, args: Array<Any?>): HookContext<Any?>? {
        val registered = HookManager.hooks[method] ?: return null
        val context = HookContext<Any?>(instance, method, args, at = HookAt.HEAD)

        for (hook in registered) {
            if (hook.at != HookAt.HEAD && hook.at != HookAt.REPLACE) continue
            hook.handler(context, context)
        }

        return context
    }

    @JvmStatic
    fun exit(method: String, instance: Any?, args: Array<Any?>, return0: Any?): Any? {
        val registered = HookManager.hooks[method] ?: return return0
        val context = HookContext<Any?>(instance, method, args, value = return0, at = HookAt.RETURN)

        for (hook in registered) {
            if (hook.at != HookAt.RETURN && hook.at != HookAt.TAIL) continue
            hook.handler(context, context)
        }

        return context.value
    }
}
