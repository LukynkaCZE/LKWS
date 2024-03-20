package responses

import com.sun.net.httpserver.HttpExchange
import java.io.File

open class Response(var httpExchange: HttpExchange) {

    var statusCode: Int = 200

    var headers: MutableList<Pair<String, String>> = mutableListOf()

    var parameters: MutableMap<String, String> = mutableMapOf()

    fun respond(message: String, statusCode: Int? = null) {
        respond(message.toByteArray(), statusCode)
    }

    fun respond(byteArray: ByteArray, statusCode: Int? = null) {
        if(statusCode != null) this.statusCode = statusCode

        val os = httpExchange.responseBody;

        headers.forEach { httpExchange.responseHeaders.add(it.first, it.second) }
        httpExchange.sendResponseHeaders(this.statusCode, byteArray.size.toLong())

        os.write(byteArray)
        os.close()
    }

    fun respondRedirect(url: String) {
        var redirectUrl = url
        if(!url.startsWith("http://") ||!url.startsWith("https://")) redirectUrl = "https://${url}"
        headers.add(Pair("Location", redirectUrl))
        respond("", 307)
    }

    fun respondFile(file: File, statusCode: Int? = null) {
        respond(file.readBytes(), statusCode)
    }
}