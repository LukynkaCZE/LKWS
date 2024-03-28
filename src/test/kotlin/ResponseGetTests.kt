import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ResponseGetTests {

    init {
        newServer()
    }

    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(6900)

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

        @JvmStatic
        @AfterAll
        fun `end server`(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test status code`() {
        val response = testRequest("/statusCode")
        assertEquals(200, response.statusCode())
    }

    @Test
    fun `test response`() {
        val response = testRequest("/response")
        assertEquals("hello world", response.body())
    }

    @Test
    fun `test url parameters`() {
        val response = testRequest("/urlparams/test123/storage/test456")
        assertEquals("test123,test456", response.body())
    }

    @Test
    fun `test query parameters`() {
        val response = testRequest("/query?client_token=abcd123456&state=logged_in")
        assertEquals("abcd123456,logged_in", response.body())
    }

    @Test
    fun `test request headers`() {
        val response = testRequest("/headers/")
        assertEquals(response.headers().map().contains("Authorization"), true)
    }

    @Test
    fun `test image`() {
        val response = testRequest("/image/")
        assertEquals(response.body(), File("src/test/imgs/test.png").readText())
    }

    @Test
    fun `test json`() {
        val response = testRequest("/json/")
        assertEquals(response.body(), File("src/test/test.json").readText())
    }
}