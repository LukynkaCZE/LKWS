import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.assertEquals

class BasicGetTest {

    private var client: HttpClient = HttpClient.newHttpClient()

    private lateinit var request: HttpRequest
    private lateinit var response: HttpResponse<String>

    init {
        TestServer
        request = HttpRequest.newBuilder()
            .uri(tUri("/basic"))
            .build()

        response = client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    @Test
    fun `test status code`() {
        assertEquals(200, response.statusCode())
    }

    @Test
    fun `test response`() {
        assertEquals("hello world", response.body())
    }

    @Test
    fun `test url parameters`() {
        
    }
}