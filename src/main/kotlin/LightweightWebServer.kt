@file:Suppress("UNCHECKED_CAST")

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import responses.ErrorResponse
import responses.GetResponse
import responses.Response
import java.net.InetSocketAddress

class LightweightWebServer(var port: Int = 7270) {

    private var server: HttpServer = HttpServer.create(InetSocketAddress(port), 0);

    companion object {
        var endpoints: MutableList<Endpoint> = mutableListOf()
        var errorResponse: ((ErrorResponse) -> Unit)? = null
    }

    init {
        server.createContext("/", Handler())
        server.executor = null
        server.start()

        println("Running Lightweight Web Server on port $port")
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

    private class Handler : HttpHandler {
        override fun handle(t: HttpExchange) {
            try {

                val path = t.requestURI.path

                // Throw if there is no endpoint with that path
                val matchingEndpoint = findMatchingEndpoint(path)
                val endpoint: Endpoint = matchingEndpoint.first ?: throw Exception("No such path as `$path` exists!")

                //Get response and execute the Unit in Endpoint whose path matches the uri path
                val response = GetResponse(t)
                response.parameters = matchingEndpoint.second
                endpoint.unit.invoke(response)

            } catch (exception: Exception) {

                // Error Response also contains the thrown exception
                val response = ErrorResponse(t, exception)

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
                        // If the segment is a replaceable, extract its value
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