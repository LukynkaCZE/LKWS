import java.io.File

fun main(args: Array<String>) {
    val server = LightweightWebServer(7270)

    server.get("/buy/{product}") { res ->
        val price = res.queryParameters["price"]?.toInt()
        val currency = res.queryParameters["currency"]
        val product = res.parameters["product"]

        val headers = res.requestHeaders["User-agent"]

        val cookies = res.requestCookies
        res.respond("You bought $product for ${price}${currency}\nUser-agent: $headers\nYour token is (plz dont steal): ${cookies["token"]}")
    }

    server.get("/gay") {
        val file = File("./imgs/test.png")
        it.respondFile(file)
    }
}