# Lightweight Kotlin Web Server

A simple, easy to use and lightweight kotlin web server for small quick projects

## Installation

<img src="https://cdn.worldvectorlogo.com/logos/kotlin-2.svg" width="16px"></img>
**Kotlin DSL**
```kotlin
repositories {
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/releases")
    }
}

dependencies {
    implementation("cz.lukynka:lkws:1.2")
}
```
<img src="https://github.com/LukynkaCZE/PrettyLog/assets/48604271/3293feca-7395-4100-8b61-257ba40dbe3c" width="18px"></img>
**Gradle Groovy**
```groovy
repositories {
    mavenCentral()
    maven {
        name "devOS"
        url "https://mvn.devos.one/releases"
    }
}

dependencies {
    implementation 'cz.lukynka:lkws:1.2'
}
```
## How to Use

You want to create new instance of `LightweightWebServer` with port supplied as parameter _(defaults to 7270)_

```kotlin
val server = LightweightWebServer(port = 7270)
```

You can then listen to get calls on specific path by calling `.get(path)` on the `server` and providing lambda expression to execute when a request is received at the specified endpoint.

```kotlin
server.get("/uwu") { res ->
    res.respond("owo :3")
}
```

you can use `Response.URLParameters[param]` to get url parameter

```kotlin
server.put("/users/{USER}/settings") {
    val user = it.URLParameters["USER"]
    val updatedSettingsJson = it.requestBody
    //handle stuff

    it.respond("settings changed", 201)
}
```

You can use `Response.requestCookies[cookie]` to get value of cookie. Will return `null` if cookie was not found. To return error to user you can simply throw exception. Error page is customizable (Error Handling section below)

```kotlin
server.post("/projects/create") {
    if(it.requestCookies["token"] != superSecretTokenTrustMe) throw Exception("nuh uh, you are not logged in")

    val projectName = it.queryParameters["name"]
    val redirectAfterCreated = it.queryParameters["redirect"].toBoolean()
    //whatever here

    if(redirectAfterCreated) {
        it.respondRedirect("/projects/$projectName")
    } else {
        it.respond("project created", 201)
    }
}
```

You can add Headers by adding `Pair<string, string>`to the `headers` variable of `Response`

```kotlin
server.get("/status") { res ->
    res.headers.Add(Pair("Content-Type", "application/json"))
    res.respondJson("{ 'status': 'operational' }")
}
```

You can also add middleware to all requests like this:

```kotlin

val token = "imdownbadforvonlycaon"

fun isAuth(response: Response): Boolean {
    return response.requestHeaders["Token"] == token
}

server.get("/status", ::isAuth) { res ->
    it.respondFile(File("src/test/imgs/lycaon.jpg"), 200, "image")
}
```

The server will automatically throw an exception if the auth function returns **false**

---

Additionally, this supports all request types (GET, POST, PUT, HEAD, PATCH etc.)

## Error Handling

Listening to errors / invalid requests is pretty much the same as listening to for example `GET` requests but with the difference of the thrown exception being included in the response object

```kotlin
server.error { res ->
    res.respond("oopsie happened: ${res.exception}", 500)
}
```
