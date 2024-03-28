import responses.ErrorResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.locks.Condition
import kotlin.concurrent.thread

fun tUri(string: String): URI {
    val endpoint = if(string.endsWith("/")) string.removeSuffix("/") else string
    return URI.create("http://127.0.0.1:6900$endpoint")
}

fun testRequest(url: String, body: String? = "", headers: MutableMap<String, String> = mutableMapOf()): HttpResponse<String> {
    val client: HttpClient = HttpClient.newHttpClient()

    val requestBuilder = HttpRequest.newBuilder()
        .uri(tUri(url))
        .method("GET", HttpRequest.BodyPublishers.ofString(body))

    headers.forEach {
        requestBuilder.setHeader(it.key, it.value)
    }

    val request = requestBuilder.build()
    return client.send(request, HttpResponse.BodyHandlers.ofString())
}

fun waitUntilNotNull(variable: Any?) {
    while(variable == null) {
        //ignore
    }
}
