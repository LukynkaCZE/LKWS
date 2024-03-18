package responses

import com.sun.net.httpserver.HttpExchange

open class Response(var httpExchange: HttpExchange) {

    var statusCode: Int = 200

    var headers: MutableList<Pair<String, String>> = mutableListOf()

    fun respond(message: String, statusCode: Int = 200) {

        this.statusCode = statusCode;

        val bodyOutputStream = httpExchange.responseBody;
        headers.forEach { httpExchange.responseHeaders.add(it.first, it.second) }
        httpExchange.sendResponseHeaders(statusCode, message.length.toLong())
        bodyOutputStream.write(message.toByteArray())
        bodyOutputStream.close()
    }

    fun respondRedirect(url: String) {
        headers.add(Pair("Location", url))
        respond("", 307)
    }
}