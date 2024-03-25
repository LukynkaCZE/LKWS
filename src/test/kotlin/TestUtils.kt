import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun tUri(string: String): URI {
    val endpoint = if(string.startsWith("/")) string.removeSuffix("/") else string
    return URI.create("http://127.0.0.1:6900$endpoint")
}


fun testRequest(url: String, body: String? = null): HttpResponse<String> {
    val client: HttpClient = HttpClient.newHttpClient()

    val request = HttpRequest.newBuilder()
        .uri(tUri(url))
        .build()

    return client.send(request, HttpResponse.BodyHandlers.ofString())
}

