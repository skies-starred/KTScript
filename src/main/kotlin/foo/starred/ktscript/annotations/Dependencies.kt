package foo.starred.ktscript.annotations

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dependencies(
    val mods: Array<String> = [],
    val minecraft: Array<String> = []
)
