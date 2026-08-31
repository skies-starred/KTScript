package foo.starred.ktscript.api.hooks.impl

import foo.starred.ktscript.KTScript
import foo.starred.ktscript.api.hooks.context.HookContext
import foo.starred.ktscript.api.hooks.data.HookAt
import foo.starred.ktscript.api.hooks.data.HookRegistration
import foo.starred.ktscript.api.hooks.data.HookTarget
import net.bytebuddy.agent.ByteBuddyAgent
import java.lang.instrument.Instrumentation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object HookManager {
    private val instrumented = ConcurrentHashMap.newKeySet<String>()

    val instrumentation: Instrumentation by lazy {
        ByteBuddyAgent.install()
    }

    val targets = CopyOnWriteArrayList<HookTarget>()
    val hooks = ConcurrentHashMap<String, CopyOnWriteArrayList<HookRegistration>>()

    init {
        try {
            instrumentation.addTransformer(HookTransformer(), true)
        } catch (e: Throwable) {
            KTScript.LOGGER.error("Failed to register HookTransformer", e)
        }
    }

    @JvmStatic
    fun register(klass: Class<*>, method: String, descriptor: String? = null, at: HookAt = HookAt.HEAD, handler: HookContext<Any?>.(HookContext<Any?>) -> Unit) {
        val target = HookTarget(klass.name, method, descriptor)
        if (targets.none { it.key == target.key }) targets.add(target)

        hooks.computeIfAbsent(target.key) { CopyOnWriteArrayList() }.add(HookRegistration(at, handler))
        if (!instrumented.add(target.key)) return

        try {
            instrumentation.retransformClasses(klass)
        } catch (e: Throwable) {
            KTScript.LOGGER.error("Failed to retransform ${target.key}", e)
        }
    }
}
