package foo.starred.ktscript.annotations

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Lib(
    val name: String = "",
    val description: String = "",
    val version: String = "1.0.0",
    val author: String = ""
)
