@file:Suppress("MemberVisibilityCanBePrivate")

package responses

import com.sun.net.httpserver.HttpExchange
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

open class Response(var httpExchange: HttpExchange) {

    // Response
    var responseStatusCode: Int = 200
    var responseHeaders: MutableList<Pair<String, String>> = mutableListOf()

    // Request
    var URLParameters: MutableMap<String, String> = mutableMapOf()
    var queryParameters: MutableMap<String, String> = mutableMapOf()
    var requestHeaders: MutableMap<String, String> = mutableMapOf()
    var requestCookies: MutableMap<String, String> = mutableMapOf()
    var requestBody: String = ""

    init {
        // Query
        val query = httpExchange.requestURI.query
        if(query != null) {
            val queryList = query.split("&")
            queryList.forEach {
                val tokens = it.split("=")
                queryParameters[tokens[0]] = tokens[1]
            }
        }

        // Headers
        val headers = httpExchange.requestHeaders
        headers.forEach { requestHeaders[it.key] = it.value.toString().removeSurrounding("[", "]") }

        // Body
        val reader = InputStreamReader(httpExchange.requestBody)
        val bufferedReader = BufferedReader(reader)
        requestBody = bufferedReader.readText()

        // Cookies
        val cookies = requestHeaders["Cookie"]
        if(cookies != null) {
            val cookiesToParse = mutableListOf<String>()

            // if contains ; we know there's multiple
            if(cookies.contains(";")) {
                val split = cookies.split("; ")
                split.forEach { cookiesToParse.add(it) }
            } else {
                cookiesToParse.add(cookies)
            }

            cookiesToParse.forEach {
                val tokens = it.split("=")
                requestCookies[tokens[0]] = tokens[1]
            }
        }
    }

    fun respond(message: String, statusCode: Int? = null) {
        respond(message.toByteArray(), statusCode)
    }

    fun respond(byteArray: ByteArray, statusCode: Int? = null) {
        if(statusCode != null) this.responseStatusCode = statusCode

        val os = httpExchange.responseBody

        responseHeaders.forEach { httpExchange.responseHeaders.add(it.first, it.second) }
        httpExchange.sendResponseHeaders(this.responseStatusCode, byteArray.size.toLong())

        os.write(byteArray)
        os.close()
    }

    fun respondRedirect(url: String) {
        val redirectUrl = if(!url.startsWith("http://") ||!url.startsWith("https://")) "https://${url}" else url
        responseHeaders.add(Pair("Location", redirectUrl))
        respond("", 307)
    }

    fun respondFile(file: File, statusCode: Int? = null, contentType: String? = null) {
        contentType?.let { addHeader("Content-type", it) }
        if(!file.exists()) throw FileNotFoundException("File ${file.path} was not found!")

        respond(file.readBytes(), statusCode)
    }

    fun respondJson(json: String) {
        addHeader("Content-type", "application/json")
        respond(json)
    }

    fun addHeader(key: String, value: String) {
        responseHeaders.add(Pair(key, value))
    }
}