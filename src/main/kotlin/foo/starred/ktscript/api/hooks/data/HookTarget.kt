package foo.starred.ktscript.api.hooks.data

data class HookTarget(
    val klass: String,
    val method: String,
    val descriptor: String? = null
) {
    val key: String by lazy {
        if (descriptor != null) "$klass#$method$descriptor" else "$klass#$method"
    }

    fun matches(name: String, desc: String): Boolean {
        if (method != name) return false
        if (descriptor != null && descriptor != desc) return false

        return true
    }
}
