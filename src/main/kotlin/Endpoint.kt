import responses.Response

data class Endpoint(
    val path: String,
    val unit: (res: Response) -> Unit,
    val type: EndpointType,
    val replaceables: MutableList<Int> = mutableListOf()
)

data class Error(
    val unit: (res: Response) -> Unit
)


enum class EndpointType {
    GET,
    PUT,
    POST,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE
}