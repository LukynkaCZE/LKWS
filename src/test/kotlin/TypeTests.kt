import cz.lukynka.lkws.LightweightWebServer
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeTests {

    init {
        newServer()
    }

    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(89)
        }

        @AfterTest
        fun endServer(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test types pass`() {
        newServer()
        server?.get("/body") {
            it.respond("yep :3")
        }
        val response = testRequest("/body", "you there?")
        assertEquals(200, response.statusCode())
        endServer()
    }

    @Test
    fun `test types fail`() {
        newServer()
        server?.post("/body") {
            it.respond("yep :3")
        }
        val response = testRequest("/body", "you there?")
        assertEquals(500, response.statusCode())
        endServer()
    }
}