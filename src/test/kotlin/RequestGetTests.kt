import kotlin.test.*

class RequestGetTests {

    init {
        newServer()
    }


    companion object {
        private var server: LightweightWebServer? = null

        private fun newServer() {
            server?.end()
            server = LightweightWebServer(6900)
        }

        @AfterTest
        fun `end server`(): Unit {
            server?.end()
        }
    }

    @Test
    fun `test body`() {
        var request: String? = null
        server?.get("/body") {
            request = it.requestBody
            it.respond("yep :3")
        }
        testRequest("/body", "you there?")

        waitUntilNotNull(request)
        assertEquals("you there?", request)
    }

    @Test
    fun `test headers`() {
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
    }

    @Test
    fun `test url params`() {
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
    }

    @Test
    fun `test query params`() {
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
    }

    @Test
    fun `test single cookie`() {
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
    }

    @Test
    fun `test cookies`() {
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
    }
}