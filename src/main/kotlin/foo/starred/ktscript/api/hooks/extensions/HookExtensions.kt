package foo.starred.ktscript.api.hooks.extensions

import foo.starred.ktscript.api.hooks.context.HookContext
import foo.starred.ktscript.api.hooks.data.HookAt
import foo.starred.ktscript.api.hooks.impl.HookManager

inline fun <reified T : Any> hook(
    method: String,
    at: HookAt = HookAt.HEAD,
    descriptor: String? = null,
    noinline handler: HookContext<Any?>.(HookContext<Any?>) -> Unit
) {
    HookManager.register(T::class.java, method, descriptor, at, handler)
}

fun hook(
    klass: String,
    method: String,
    at: HookAt = HookAt.HEAD,
    descriptor: String? = null,
    handler: HookContext<Any?>.(HookContext<Any?>) -> Unit
) {
    HookManager.register(Class.forName(klass), method, descriptor, at, handler)
}
