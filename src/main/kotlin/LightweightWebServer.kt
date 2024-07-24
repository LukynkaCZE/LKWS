@file:Suppress("UNCHECKED_CAST")

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import responses.ErrorResponse
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
            log(ex)
            throw Exception(ex.message)
        }

        server.createContext("/", Handler())
        server.executor = null
        server.start()

        log("Running Lightweight Web Server on port $port", LogType.SUCCESS)
    }

    fun end(exitCode: Int = 0) {
        server.stop(exitCode)
        log("Lightweight Web Server stopped with exit code $exitCode", LogType.ERROR)
    }

    private fun path(path: String, type: EndpointType, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        val endpointPath = if(path.startsWith("/")) path else "/$path"
        endpointPath.removeSuffix("/")

        val tokens = endpointPath.split("/")
        val urlParams = mutableListOf<Int>()

        tokens.forEachIndexed { index, it ->
            if(it.startsWith("{") && it.endsWith("}"))
                urlParams.add(index)
        }

        endpoints.add(Endpoint(endpointPath, function, type, urlParams, auth))
    }

    fun get(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.GET, null, function)
    }
    fun put(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.PUT, null, function)
    }
    fun post(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.POST, null, function)
    }
    fun patch(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.PATCH, null, function)
    }
    fun delete(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.DELETE, null, function)
    }
    fun options(path: String, function: (res: Response) -> Unit) {
        path(path, EndpointType.OPTIONS, null, function)
    }

    fun get(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.GET, auth, function)
    }
    fun put(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.PUT, auth, function)
    }
    fun post(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.POST, auth, function)
    }
    fun patch(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.PATCH, auth, function)
    }
    fun delete(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.DELETE, auth, function)
    }
    fun options(path: String, auth: ((Response) -> Boolean)?, function: (res: Response) -> Unit) {
        path(path, EndpointType.OPTIONS, auth, function)
    }

    fun error(function: (res: ErrorResponse) -> Unit) {
        errorResponse = function
    }

    inner class Handler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                val path = exchange.requestURI.path

                val type = EndpointType.valueOf(exchange.requestMethod);

                // Throw if there is no endpoint with that path
                val matchingEndpoint = findMatchingEndpoint(path, type)

                if(matchingEndpoint.first == null || matchingEndpoint.first!!.type != type) throw Exception("No such path as `$path` exists for type $type")
                val endpoint = matchingEndpoint.first!!

                val response = Response(exchange)

                //Get response and execute the Unit in Endpoint whose path matches the uri path
                response.URLParameters = matchingEndpoint.second
                val auth = endpoint.auth
                if(auth != null && !auth(response)) throw Exception("Not authorized")
                endpoint.unit.invoke(response)

            } catch (exception: Exception) {

                // Error Response also contains the thrown exception
                val response = ErrorResponse(exchange, exception)

                // Default response if `errorResponse` is not set by the user
                if(errorResponse == null) {
                    response.respond("Something went wrong: $exception", 500)
                    var isPathError = false
                    if(exception.message != null) {
                        val split = exception.message!!.split("`")
                        if (split[0].contains("No such path as")) {
                            val splitType = split[2].split("type ")
                            log("${exchange.remoteAddress} tried to access ${split[1]} but that path with type ${splitType[1]} does not exist", LogType.WARNING)
                            isPathError = true
                        }
                    }
                    if(!isPathError) {
                        log(exception)
                    }
                    return
                }

                errorResponse!!.invoke(response)
            }
        }

        private fun findMatchingEndpoint(path: String, type: EndpointType): Pair<Endpoint?, MutableMap<String, String>> {
            val replaceableValues = mutableMapOf<String, String>()

            val matchedEndpoint = endpoints.firstOrNull { endpoint ->
                if (endpoint.type != type) {
                    return@firstOrNull false
                }

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