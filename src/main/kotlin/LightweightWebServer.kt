@file:Suppress("UNCHECKED_CAST")

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import responses.ErrorResponse
import responses.GetResponse
import responses.Response
import java.net.InetSocketAddress

class LightweightWebServer(port: Int = 7270) {

    private var server: HttpServer

    var endpoints: MutableList<Endpoint> = mutableListOf()
    var errorResponse: ((ErrorResponse) -> Unit)? = null

    init {
        try {
            server = HttpServer.create(InetSocketAddress(port), 0)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw Exception(ex.message)
        }

        server.createContext("/", Handler())
        server.executor = null
        server.start()

        println("Running Lightweight Web Server on port $port")
    }

    fun end(exitCode: Int = 0) {
        server.stop(exitCode)
        println("Lightweight Web Server stopped with exit code $exitCode")
    }

    fun get(path: String, function: (res: GetResponse) -> Unit) {
        val endpointPath = if(path.startsWith("/")) path else "/$path"
        endpointPath.removeSuffix("/")
        val tokens = endpointPath.split("/")
        val replacables = mutableListOf<Int>()
        tokens.forEachIndexed { index, it ->
            if(it.startsWith("{") && it.endsWith("}"))
            replacables.add(index)
        }
        endpoints.add(Endpoint(endpointPath, function as (Response) -> Unit, EndpointType.GET, replacables))
    }

    fun error(function: (res: ErrorResponse) -> Unit) {
        errorResponse = function
    }

    inner class Handler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                val path = exchange.requestURI.path

                // Throw if there is no endpoint with that path
                val matchingEndpoint = findMatchingEndpoint(path)
                val endpoint: Endpoint = matchingEndpoint.first ?: throw Exception("No such path as `$path` exists!")

                val response = when(endpoint.type) {
                    EndpointType.GET -> GetResponse(exchange)
                    else -> Response(exchange)
                }

                //Get response and execute the Unit in Endpoint whose path matches the uri path
                response.URLParameters = matchingEndpoint.second
                endpoint.unit.invoke(response)

            } catch (exception: Exception) {

                // Error Response also contains the thrown exception
                val response = ErrorResponse(exchange, exception)

                // Default response if `errorResponse` is not set by the user
                if(errorResponse == null) {
                    response.respond("Something went wrong: $exception", 500)
                    return
                }

                errorResponse!!.invoke(response)
            }
        }

        private fun findMatchingEndpoint(path: String): Pair<Endpoint?, MutableMap<String, String>> {
            val replaceableValues = mutableMapOf<String, String>()

            val matchedEndpoint = endpoints.firstOrNull { endpoint ->
                val endpointPath = endpoint.path.split("/")
                val requestPath = path.split("/")

                if (endpointPath.size != requestPath.size) {
                    return@firstOrNull false
                }

                endpointPath.zip(requestPath).all { (endpointSegment, requestSegment) ->
                    if (endpointSegment.startsWith("{") && endpointSegment.endsWith("}")) {
                        val replaceableKey = endpointSegment.removeSurrounding("{", "}")
                        replaceableValues[replaceableKey] = requestSegment
                        true
                    } else {
                        endpointSegment == requestSegment
                    }
                }
            }
            return Pair(matchedEndpoint, replaceableValues)
        }
    }
}