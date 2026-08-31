package foo.starred.ktscript.annotations

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Import(vararg val paths: String)
