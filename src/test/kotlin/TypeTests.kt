import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TypeTests {

    init {
        newServer()
    }

    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(6900)
        }

        @JvmStatic
        @AfterAll
        fun `end server`(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test types pass`() {
        server?.get("/body") {
            it.respond("yep :3")
        }
        val response = testRequest("/body", "you there?")
        assertEquals(200, response.statusCode())
    }

    @Test
    fun `test types fail`() {
        server?.post("/body") {
            it.respond("yep :3")
        }
        val response = testRequest("/body", "you there?")
        assertEquals(500, response.statusCode())
    }
}