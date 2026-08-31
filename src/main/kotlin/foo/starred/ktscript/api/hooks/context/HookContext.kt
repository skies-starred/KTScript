package foo.starred.ktscript.api.hooks.context

import foo.starred.ktscript.api.hooks.data.HookAt

class HookContext<T>(
    @JvmField
    val instance: Any?,
    @JvmField
    val method: String,
    @JvmField
    val args: Array<Any?>,
    @JvmField
    var value: T? = null,
    @JvmField
    var cancelled: Boolean = false,
    @JvmField
    val at: HookAt = HookAt.HEAD
) {
    fun cancel(t: T? = null) {
        cancelled = true
        if (t != null) value = t
    }

    inline fun <reified I> instance(): I {
        return instance as I
    }

    inline operator fun <reified A> get(index: Int = 0): A {
        return args[index] as A
    }

    operator fun set(index: Int, value: Any?) {
        args[index] = value
    }

    inline operator fun <reified A> component1(): A {
        return args[0] as A
    }

    inline operator fun <reified A> component2(): A {
        return args[1] as A
    }

    inline operator fun <reified A> component3(): A {
        return args[2] as A
    }

    inline operator fun <reified A> component4(): A {
        return args[3] as A
    }

    inline operator fun <reified A> component5(): A {
        return args[4] as A
    }

    inline operator fun <reified A> component6(): A {
        return args[5] as A
    }

    inline operator fun <reified A> component7(): A {
        return args[6] as A
    }

    inline operator fun <reified A> component8(): A {
        return args[7] as A
    }

    inline operator fun <reified A> component9(): A {
        return args[8] as A
    }

    inline operator fun <reified A> component10(): A {
        return args[9] as A
    }

    inline operator fun <reified A> component11(): A {
        return args[10] as A
    }

    inline operator fun <reified A> component12(): A {
        return args[11] as A
    }

    inline operator fun <reified A> component13(): A {
        return args[12] as A
    }

    inline operator fun <reified A> component14(): A {
        return args[13] as A
    }

    inline operator fun <reified A> component15(): A {
        return args[14] as A
    }

    inline operator fun <reified A> component16(): A {
        return args[15] as A
    }
}
