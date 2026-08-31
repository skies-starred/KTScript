package foo.starred.ktscript.api.network

import foo.starred.ktscript.KTScript
import foo.starred.snowbird.api.network.WebUtils
import foo.starred.snowbird.api.network.builders.impl.DownloadBuilder
import foo.starred.snowbird.api.network.builders.impl.RequestBuilder
import foo.starred.snowbird.api.network.data.HttpRequest
import java.io.File

object WebAPI : WebUtils("ktscript", KTScript.LOGGER)

fun String.request(type: HttpRequest = HttpRequest.GET, log: Boolean = true, block: RequestBuilder.() -> Unit = {}) {
    with(WebAPI) { request(type, log, block) }
}

fun String.download(output: File, log: Boolean = true, block: DownloadBuilder.() -> Unit = {}) {
    with(WebAPI) { download(output, log, block) }
}
