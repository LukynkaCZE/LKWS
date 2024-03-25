import java.io.File
import java.nio.file.Paths

object TestServer {

    var server: LightweightWebServer = LightweightWebServer(6900)

    init {
        server.get("/statusCode") {
            it.respond("200", 200)
        }

        server.get("/response") {
            it.respond("hello world", 200)
        }

        server.get("/urlparams/{1}/storage/{2}") {
            it.respond("${it.parameters["1"]},${it.parameters["2"]}")
        }

        server.get("/query") {
            it.respond("${it.queryParameters["client_token"]},${it.queryParameters["state"]}")
        }

        server.get("/headers") {
            it.addHeader("Authorization", "abc123")
            it.respond("uwu")
        }

        server.get("/image") {
            it.respondFile(File("src/test/imgs/test.png"))
        }

        server.get("/json") {
            it.respondJson(File("src/test/test.json").readText())
        }
    }
}