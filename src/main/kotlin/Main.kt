fun main(args: Array<String>) {
    val server = LightweightWebServer(7270)

    server.get("/test") { res ->
        res.respondRedirect("a")
    }

    server.get("") {res ->
        res.respond("hello hi :3")
    }

//    server.error { res ->
//        res.respond("uh oh", 500)
//    }
}