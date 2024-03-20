import java.net.URI

fun tUri(string: String): URI {
    val endpoint = if(string.startsWith("/")) string.removeSuffix("/") else string
    return URI.create("http://127.0.0.1:6900$endpoint")
}