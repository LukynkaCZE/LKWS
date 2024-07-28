import cz.lukynka.lkws.LightweightWebServer
import org.junit.After
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseGetTests {

    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(89)

            server?.get("/statusCode") {
                it.respond("200", 200)
            }

            server?.get("/response") {
                it.respond("hello world", 200)
            }

            server?.get("/urlparams/{1}/storage/{2}") {
                it.respond("${it.URLParameters["1"]},${it.URLParameters["2"]}")
            }

            server?.get("/query") {
                it.respond("${it.queryParameters["client_token"]},${it.queryParameters["state"]}")
            }

            server?.get("/headers") {
                it.addHeader("Authorization", "abc123")
                it.respond("uwu")
            }

            server?.get("/image") {
                it.respondFile(File("src/test/imgs/test.png"))
            }

            server?.get("/json") {
                it.respondJson(File("src/test/test.json").readText())
            }

        }

        @After
        fun endServer(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test status code`() {
        newServer()
        val response = testRequest("/statusCode")
        assertEquals(200, response.statusCode())
        endServer()
    }

    @Test
    fun `test response`() {
        newServer()
        val response = testRequest("/response")
        assertEquals("hello world", response.body())
        endServer()
    }

    @Test
    fun `test url parameters`() {
        newServer()
        val response = testRequest("/urlparams/test123/storage/test456")
        assertEquals("test123,test456", response.body())
        endServer()
    }

    @Test
    fun `test query parameters`() {
        newServer()
        val response = testRequest("/query?client_token=abcd123456&state=logged_in")
        assertEquals("abcd123456,logged_in", response.body())
        endServer()
    }

    @Test
    fun `test request headers`() {
        newServer()
        val response = testRequest("/headers/")
        assertEquals(response.headers().map().contains("Authorization"), true)
        endServer()
    }

    @Test
    fun `test image`() {
        newServer()
        val response = testRequest("/image/")
        assertEquals(response.body(), File("src/test/imgs/test.png").readText())
        endServer()
    }

    @Test
    fun `test json`() {
        newServer()
        val response = testRequest("/json/")
        assertEquals(response.body(), File("src/test/test.json").readText())
        endServer()
    }
}