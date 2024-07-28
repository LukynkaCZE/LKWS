package cz.lukynka.lkws.responses

import com.sun.net.httpserver.HttpExchange

class ErrorResponse(httpExchange: HttpExchange, var exception: Exception) : Response(httpExchange) {

}