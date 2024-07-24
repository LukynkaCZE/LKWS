import cz.lukynka.prettylog.log
import org.junit.After
import responses.Response
import java.io.File
import kotlin.test.*

class RequestGetTests {

    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(6900)
        }

        @After
        fun endServer(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test body`() {
        newServer()
        var request: String? = null
        server?.get("/body") {
            request = it.requestBody
            it.respond("yep :3")
        }
        testRequest("/body", "you there?")

        waitUntilNotNull(request)
        assertEquals("you there?", request)
        endServer()
    }

    @Test
    fun `test headers`() {
        newServer()
        var headers: MutableMap<String, String>? = null
        server?.get("/headers") {
            headers = it.requestHeaders
            it.respond(":3")
        }
        testRequest("/headers", "", mutableMapOf(Pair("Content-type", "weird/stuff"), Pair("Authorization", "admin")))
        waitUntilNotNull(headers)

        assertContains(headers!!, "Content-type")
        assertContains(headers!!, "Authorization")
        assertEquals("weird/stuff", headers!!["Content-type"])
        assertEquals("admin", headers!!["Authorization"])
        endServer()
    }

    @Test
    fun `test url params`() {
        newServer()
        var urlParams: MutableMap<String, String>? = null
        server?.get("/urlparams/{username}/data/{password}") {
            urlParams = it.URLParameters
            it.respond(":3")
        }
        testRequest("/urlparams/LukynkaCZE/data/colonthree")

        waitUntilNotNull(urlParams)

        assertContains(urlParams!!, "username")
        assertContains(urlParams!!, "password")
        assertEquals("LukynkaCZE", urlParams!!["username"])
        assertEquals("colonthree", urlParams!!["password"])
        endServer()
    }

    @Test
    fun `test query params`() {
        newServer()
        var queryParams: MutableMap<String, String>? = null
        server?.get("/queryParams") {
            queryParams = it.queryParameters
            it.respond(":3")
        }
        testRequest("/queryParams?token=Z5Z5HdAmH8JxpLr&login_state=1")

        waitUntilNotNull(queryParams)

        assertContains(queryParams!!, "token")
        assertContains(queryParams!!, "login_state")
        assertEquals("Z5Z5HdAmH8JxpLr", queryParams!!["token"]!!)
        assertEquals("1", queryParams!!["login_state"])
        endServer()
    }

    @Test
    fun `test single cookie`() {
        newServer()
        var cookie: MutableMap<String, String>? = null
        server?.get("/cookie") {
            cookie = it.requestCookies
            it.respond(":3")
        }
        testRequest("/cookie", "", mutableMapOf(Pair("Cookie", "token=Z5Z5HdAmH8JxpLr")))

        waitUntilNotNull(cookie)

        assertContains(cookie!!, "token")
        assertNotEquals("false", cookie!!["accepted_cookies"])
        assertEquals("Z5Z5HdAmH8JxpLr", cookie!!["token"])
        endServer()
    }

    @Test
    fun `test cookies`() {
        newServer()
        var cookies: MutableMap<String, String>? = null
        server?.get("/cookies") {
            cookies = it.requestCookies
            it.respond(":3")
        }
        testRequest("/cookies", "", mutableMapOf(Pair("Cookie", "token=Z5Z5HdAmH8JxpLr; accepted_cookies=false")))

        waitUntilNotNull(cookies)

        assertContains(cookies!!, "token")
        assertContains(cookies!!, "accepted_cookies")
        assertEquals("false", cookies!!["accepted_cookies"])
        assertEquals("Z5Z5HdAmH8JxpLr", cookies!!["token"])
        endServer()
    }

    @Test
    fun `test middleware`() {
        newServer()
        val token = "imdownbadforvonlycaon"

        fun isAuth(response: Response): Boolean {
            log(response.requestHeaders.toString())
            return response.requestHeaders["Token"] == token
        }

        val server = LightweightWebServer(6969)
        server.get("/lycaon", ::isAuth) {
            it.respondFile(File("src/test/imgs/lycaon.jpg"), 200, "image")
        }
        val reqWithoutToken = testRequest("/lycaon")
        val reqWithToken = testRequest("/lycaon", "", mutableMapOf(Pair("Token", "imdownbadforvonlycaon")))

        assert(reqWithoutToken.statusCode() != 200)
        assert(reqWithToken.statusCode() != 200)

        endServer()
    }
}