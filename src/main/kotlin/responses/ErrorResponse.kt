package responses

import com.sun.net.httpserver.HttpExchange

class ErrorResponse(httpExchange: HttpExchange, var exception: Exception) : Response(httpExchange) {

}