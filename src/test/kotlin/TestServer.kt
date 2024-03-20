object TestServer {

    init {
        val server = LightweightWebServer(6900)

        server.get("/basic") {
            it.respond("hello world", 200)
        }
    }

}