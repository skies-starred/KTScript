package foo.starred.ktscript.api.hooks.data

import foo.starred.ktscript.api.hooks.context.HookContext

data class HookRegistration(
    val at: HookAt,
    val handler: HookContext<Any?>.(HookContext<Any?>) -> Unit
)
