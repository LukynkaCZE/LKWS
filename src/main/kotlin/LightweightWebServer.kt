@file:Suppress("UNCHECKED_CAST")

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import responses.ErrorResponse
import responses.GetResponse
import responses.Response
import java.net.InetSocketAddress

class LightweightWebServer(var port: Int) {

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
        endpoints.add(Endpoint(endpointPath, function as (Response) -> Unit, EndpointType.GET))
    }

    fun error(function: (res: ErrorResponse) -> Unit) {
        errorResponse = function
    }



    private class Handler : HttpHandler {
        override fun handle(t: HttpExchange) {
            try {

                val path = t.requestURI.rawPath

                // Throw if there is no endpoint with that path
                if (!endpoints.any { it.path == path }) throw Exception("No such path as `$path` exists!")

                //Get response and execute the Unit in Endpoint whose path matches the uri path
                val response = GetResponse(t)
                endpoints.firstOrNull { it.path == path }?.unit?.invoke(response)

            } catch (exception: Exception) {

                // Error Response also contains the thrown exception
                val response = ErrorResponse(t, exception)

                // Default response if `errorResponse` is not set by the user
                if(errorResponse == null) {
                    response.respond("Something went wrong: $exception", 200)
                    return
                }

                errorResponse!!.invoke(response)
            }
        }
    }
}