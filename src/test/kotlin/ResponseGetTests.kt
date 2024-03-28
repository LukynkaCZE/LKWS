import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ResponseGetTests {
    init {
        TestServer
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

    companion object {
        @JvmStatic
        @AfterAll
        fun `end server`(): Unit {
            TestServer.server.end()
        }
    }
}